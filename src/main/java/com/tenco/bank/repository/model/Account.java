package com.tenco.bank.repository.model;

import java.sql.Timestamp;

import com.tenco.bank.handler.exception.DataDeliveryException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

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
    public boolean checkPassword(String password) {
        boolean isOk = true;
        if (this.password.equals(password) == false) {
            // 사용자한테 비밀번호 틀렸어요
            isOk = false;
            throw new DataDeliveryException("계좌 비밀번호가 틀렸어요.", HttpStatus.BAD_REQUEST);
        }
        return isOk;
    }

    // 잔액 여부 확인 기능
    public void checkBalance(Long amount){
        if(this.balance < amount){
            // 내 잔액보다 더 많은 금액을 출금 하려고 하면 global 거기로 exception 으로 던져 버려서 리턴이 필요없음
            throw new DataDeliveryException("잔액이 부족합니다.", HttpStatus.BAD_REQUEST);

        }
    }



    // 계좌 소유자 확인 기능
    public void checkOwner(Integer pricipalId){
        if(this.userId != pricipalId){
            throw new DataDeliveryException("본인 계좌가 아닙니다.                         ", HttpStatus.BAD_REQUEST);

        }
    }
}