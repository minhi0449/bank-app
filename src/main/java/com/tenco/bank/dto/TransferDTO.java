package com.tenco.bank.dto;

import lombok.Data;

/*
    날짜 : 2025.02.17 (월)
    이름 : 김민희
    내용 :
 */

@Data // getter,setter 가 반드시 필요함
public class TransferDTO {

    private Long amount;
    private String wAccountNumber;
    private String dAccountNumber;
    private String password; // 출금 계좌 비밀번호
}