package com.tenco.bank.handler.exception;

import org.apache.logging.log4j.message.Message;
import org.springframework.http.HttpStatus;

// 인증되지 않은 사용자가 인증이 필요한 페이지에 접속할 때, 예외처리

// 사용자 정의 예외 클래스 만들기
public class UnAuthorizedException extends RuntimeException {

    private HttpStatus status;

    // 예외 발생했을 때, --> HTTP 상태코드를 알려 준다.
    // 메세지 (어떤 예외 발생)
    public UnAuthorizedException (String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}