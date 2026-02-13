package com.health.community.common.exception;

public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super(400, message);
    }
}