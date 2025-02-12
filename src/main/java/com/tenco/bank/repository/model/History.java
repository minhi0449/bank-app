package com.tenco.bank.repository.model;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
    날짜 : 2025.02.12 (수)
    이름 : 김민희
    내용 : 은행 거래 내역을 저장하는 모델 클래스
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class History {

    private Integer id;
    private Long amount;
    private Long wBalance;
    private Long dBalance;
    private Integer wAccountId;
    private Integer dAccountId;
    private Timestamp createdAt;
}