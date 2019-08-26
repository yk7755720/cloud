package com.common.freamwork;

public class BusinessException extends RuntimeException {

    private int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = BusinessResult.BUSINESS_FAILED_CODE;
    }
    
    public BusinessException(String message, Throwable e) {
    	super(message, e);
    	this.code = BusinessResult.BUSINESS_FAILED_CODE;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}
