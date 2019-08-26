package com.common.freamwork;


public class SystemException extends RuntimeException {

    private int code;

    public SystemException(int code, String message) {
        super(message);
        this.code = code;
    }

    public SystemException(String message) {
        super(message);
        this.code = BusinessResult.SYSTEM_FAILED_CODE;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}
