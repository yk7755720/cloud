package com.common.freamwork;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "资源不存在")
public class NoResourceException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int code;

    public NoResourceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public NoResourceException(String message) {
        super(message);
        this.code = BusinessResult.BUSINESS_FAILED_CODE;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}
