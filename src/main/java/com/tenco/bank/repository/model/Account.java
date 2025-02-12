package com.tenco.bank.repository.model;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
    날짜 : 2025.02.12 (수)
    이름 : 김민희
    내용 :
 */

// Account Entity 를 설계 중입니다. -> MySQL 에서는 엔티티라고 잘 말하지 않음
// Enitity 로 사용하는 클래스는 로직을 포함 할 수 있다.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    private Integer id;
    private String number;
    private String password;
    private Long balance;
    private Integer userId;
    private Timestamp createdAt;

    // 출금 기능
    public void withdraw(Long amount) {
        // 방어적 코드 작성 예정
        // 어떠한 방어적 코드가 필요하죠?
        // 내 잔액보다 더 많은 금액을 출금하면 안 되겠져?
        this.balance -= amount;
    }
    // 입금 기능
    public void deposit(Long amount) {
        this.balance += amount;
    }

    // TODO - 추후 추가
    // 패스워드 체크 기능
    // 잔액 여부 확인 기능
    // 계좌 소유자 확인 기능

}