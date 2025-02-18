package com.tenco.bank.dto;

import lombok.Data;

import java.sql.Timestamp;

/*
    날짜 : 2025.02.17 (월)
    이름 : 김민희
    내용 :
 */

// JOIN 결과를 매핑할 모델은 DTO로 설계하는 것이 일반적이다.
@Data
public class HistoryAccountDTO {
    private Integer id;
    private Long amount;
    private Long balance;
    private String sender;
    private String receiver;
    private Timestamp createdAt;
    // map-underscore-to-camel-case: true 설정해놔서
    // createdAt
}
