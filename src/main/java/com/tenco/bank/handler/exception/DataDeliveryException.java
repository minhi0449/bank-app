package com.tenco.bank.handler.exception;

import org.apache.logging.log4j.message.Message;
import org.springframework.http.HttpStatus;

// 사용자 정의 예외 클래스 만들기
public class DataDeliveryException extends RuntimeException {

    private HttpStatus status;

    // 예외 발생했을 때, --> HTTP 상태코드를 알려 준다.
    // 어떤 예외 발생했는지 상태 메시지를 알려준다.
    // 메세지 (어떤 예외 발생)
    public DataDeliveryException (String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}