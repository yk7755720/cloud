package com.common.token;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.common.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * @Author liuxiaoxin
 * @Descrpition
 * @Date 2018/4/30
 * @Modified By:
 */
@Component("jwtTokenService")
@PropertySource(value = "classpath:config/config.properties")
public class JwtTokenService {

    @Value("${jwt.expires.min}")
    private int expiresMinute;

    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * 生成Token
     *
     * @param json
     * @return
     * @throws Exception
     */
    public String createToken(JSONObject json) {
        return JWTUtils.createToken(json, expiresMinute, secretKey);
    }

    /**
     * 过期Token去Redis中校验，符合校验返回新Token
     *
     * @param token
     * @return
     * @throws Exception
     */
    public String verifyRefreshToken(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        //token失效
        if (decodedJWT.getExpiresAt().before(new Date())) {
            throw new TokenExpiredException("登陆信息已过期");
        }
    	String userKey = getTokenUserKey(token);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userkey", userKey);
        return createToken(jsonObject);
    }

    public String getTokenUserKey(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        Map<String, Claim> claimMap = decodedJWT.getClaims();
        return claimMap.get("userkey").asString();
    }
}
