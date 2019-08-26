package com.common.freamwork;

public class BusinessResult {
    public static final int SUCCESS_CODE = 10000;//成功
    public static final int BUSINESS_FAILED_CODE = 10001; //业务失败
    public static final int SYSTEM_FAILED_CODE = 10002;   //系统失败
    public static final int UNAUTHORIZED_CODE = 10003;    //鉴权失败
    public static final int REQUESTNULL_CODE = 10005;     //请求参数不能为空
    public static final int RESULTNULL_CODE = 10006;      //结果为空
    public static final int ACCOUNTORPSDWRONG = 10009;      //账号或密码错误

    private int code;
    private String msg;
    private String token;
    private Object data;
    private Long stamp;

    public static BusinessResult success() {
        return success("", "", "");
    }

    public static BusinessResult success(Object data) {
        return success("", data, "");
    }

    public static BusinessResult success(Object data, String token) {
        return success("", data, token);
    }

    public static BusinessResult success(String message, Object data, String token) {
        BusinessResult result = new BusinessResult();
        result.setCode(SUCCESS_CODE);
        result.setMsg(message);
        result.setStamp(System.currentTimeMillis());
        result.setData(data);
        result.setToken(token);
        return result;
    }

    public static BusinessResult failed(String message) {
        return failed(BUSINESS_FAILED_CODE, message);
    }

    public static BusinessResult failed(int code, String message) {
        BusinessResult result = new BusinessResult();
        result.setCode(code);
        result.setMsg(message);
        result.setStamp(System.currentTimeMillis());
        result.setData("");
        return result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Long getStamp() {
        return stamp;
    }

    public void setStamp(Long stamp) {
        this.stamp = stamp;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
