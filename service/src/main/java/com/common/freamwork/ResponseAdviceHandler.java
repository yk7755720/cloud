package com.common.freamwork;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;

/**
 * @Author liuxiaoxin
 * @Descrpition
 * @Date 2018/5/4
 * @Modified By:
 */
public class ResponseAdviceHandler extends RestExceptionHandler implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    /**
     * 返回前回写Token，保证点击
     *
     * @param o
     * @param methodParameter
     * @param mediaType
     * @param aClass
     * @param serverHttpRequest
     * @param serverHttpResponse
     * @return
     */
    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (o instanceof BusinessResult) {
            BusinessResult businessResult = (BusinessResult) o;
            try {
                if (BusinessResult.SUCCESS_CODE == businessResult.getCode()) {
                    HttpHeaders httpHeaders = serverHttpResponse.getHeaders();
                    List<String> tokenList = httpHeaders.get("refresh-token");
                    if (!CollectionUtils.isEmpty(tokenList)) {
                        businessResult.setToken(tokenList.get(0));
                        return businessResult;
                    }
                } else {
                    HttpHeaders reqHeaders = serverHttpRequest.getHeaders();
                    List<String> tokenList = reqHeaders.get("token");
                    if (!CollectionUtils.isEmpty(tokenList)) {
                        businessResult.setToken(tokenList.get(0));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return o;
    }
}
