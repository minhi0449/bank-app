package com.tenco.bank.service;

import com.tenco.bank.dto.DepositDTO;
import com.tenco.bank.dto.WithdrawalDTO;
import com.tenco.bank.repository.interfaces.HistoryRepository;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.History;
import com.tenco.bank.utils.Define;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tenco.bank.dto.AccountSaveDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.AccountRepository;

import java.util.List;

/*
  날짜 : 2025.02.13 (목)
  이름 : 김민희
  내용 : 계좌 생성을 위한 AccountService 설계

  수정 : 2025.02.14 (금) - 김민희 : 입/출금 기능 추가
 */


@Service
@Log4j2
public class AccountService {

    @Autowired
    private final AccountRepository accountRepository;
    private final HistoryRepository historyRepository;
    // 인터페이스로 받아서 처리해주는 게 좋다
    public AccountService(AccountRepository accountRepository, HistoryRepository historyRepository) {
        this.accountRepository = accountRepository;
        this.historyRepository = historyRepository;
    }

    /**
     * 계좌 생성 기능
     *
     * @param dto
     * @param pricipalId
     */
    @Transactional
    public void createAccount(AccountSaveDTO dto, Integer pricipalId) {

        try {
            // 바로 save, 조회 하고 계좌 존재 시 알려주기
            accountRepository.insert(dto.toAccount(pricipalId));
        } catch (DataAccessException e) {
            // DB 연결 및 제약 사항 위한 및 쿼리 오류
            throw new DataDeliveryException("잘못된 처리 입니다", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // 예외 처리 - 에러 페이지로 이동
            System.out.println(e.getMessage()); // 에러 메시지 자세히 찍기
            // throw new RedirectException(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
            throw new RedirectException("알 수 없는 오류 입니다.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }


    /**
     * 복잡한 Select 쿼리문일 경우 트랜잭션 처리를 해주 것이 좋습니다.
     * 여기서는 단순한 Select 구문이라 바로 진행 합니다.
     * @param principalId
     * @return
     */
    // 계좌 목록 페이지
    public List<Account> readAccountListByUserId(Integer principalId) {
        List<Account> accountListEntity = null;
        try {
            accountListEntity = accountRepository.findAllByUserId(principalId);
        } catch (DataAccessException e) {
            throw new DataDeliveryException("잘못된 처리 입니다", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // 예외 처리 - 에러 페이지로 이동
            throw new RedirectException("알 수 없는 오류", HttpStatus.SERVICE_UNAVAILABLE);
        }
        return accountListEntity;
    }

    // 아래 로직을 스스로 생각해보는 연습이 필요한 수업이었음 짜잔
    // 출금 -->
    // 1. 트랜잭션 처리
    // 2. 계좌 번호 존재 여부 확인 -- select
    // 3. 본인 계좌 연부 확인 -- 객체에서 확인 가능
    // 4. 계좌의 비밀번호 확인
    // 5. 잔액 여부 확인(출금 가능 금액)
    // 6. 출금 처리 ----> update (해당하는 데이터베이스 처리)
    // 7. 거래 내역 등록 ----> insert (history) [트랜잭션을 거는 이유가 ]\
    @Transactional
    public void updateAccountWithdraw(WithdrawalDTO dto, Integer principalId) {
        // 잔액 1,000 원
        Account account = accountRepository.findByNumber(dto.getWAccountNumber());
        if(account == null){
            // 계좌번호가 없는 상황이라면 예외 던지기
            throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST); // 존재가 없으면 던져 버리기
            // 본인 계좌가 아니면 던져 버리기 (사용자 것이 아니니까 Bad 리퀘스트)
            // account.getUserId ~ 사용하지 말고 , Account.java 에 작성해둔 checkOwner 메서드 사용하기
        }
        account.checkOwner(principalId); // 아니라면 E, H 실행
        account.checkPassword(dto.getWAccountPassword());
        account.checkBalance(dto.getAmount()); // 객체 상태 변경
        account.withdraw(dto.getAmount()); // 출금 처리 <-- 여기가 빠지면 출금 되지 않은 채 업데이트
        accountRepository.updateById(account);
        // History table -> insert 처리
        // History 객체 생성해서 널기
        History history = new History();
        history.setAmount(dto.getAmount()); // 거래 내역이 일어났어
        history.setWBalance(account.getBalance()); // 잔액 여부 -> 어디서 데이터를 넎어줘야 해? ->
        history.setDBalance(null); // 출금이니까 (입금 값은 null)
        history.setWAccountId(account.getId());
        history.setDAccountId(null); // 입금 Id 는 null
        // 히스토리 레파지토리 필요
        int rowResetCount = historyRepository.insert(history);
        System.out.println("111111 : " + rowResetCount);
        if(rowResetCount != 1){
            throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    // 입금 기능
    // 1. 트랜잭션
    // 2. 계좌 존재 여부 확인 --> select --> Account 모델 리턴
    // 3. 본인 계좌 여부 확인 --> 객체 상태 값에서 확인 가능
    // 4. 입금 처리 --> update
    // 5. 거래 내역 등록 --> history table --> insert
    @Transactional
    public void updateAccountDeposit(DepositDTO dto, Integer principalId) {

        // 2. 계좌 존재 여부 확인 (입금 deposit)
        Account account = accountRepository.findByNumber(dto.getDAccountNumber());
        if(account == null){
            throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
        }

        // 3. 본인 계좌 여부 확인
        account.checkOwner(principalId); // principalId : 계좌 소유자 Id
        // ** 비밀번호 필요 ❌ **

        // 4. 입금 처리 (update)
        account.deposit(dto.getAmount()); // 금액 +

        // 4-1. 입금 후 잔액
        account.checkBalance(dto.getAmount()); // 입금 후 잔액 상태 변경

        // 4-2. 입금 잔액 업데이트 처리
        accountRepository.updateById(account);

        // 5. 거래 내역 (History)
        History history = new History();
        history.setAmount(dto.getAmount()); // 거래내역 발생
        history.setDBalance(account.getBalance()); // 잔액 여부 확인 (입금 D의 balance 확인)
        history.setWBalance(null); // 출금 잔액 null
        history.setDAccountId(account.getId()); // 이거는 입금 아이디로 입금 하겠다? 입금 내역 찍히는 ID?
        history.setWAccountId(null); // 출금처리는 안 했으니까 null

        // historyRepository 필요 --> 왜 필요할까? ( rowResetCount )
        int rowResetCount = historyRepository.insert(history); // 거래 내역에 입/출금 내역 찍어야 해서
        if(rowResetCount != 1){
            throw new DataDeliveryException(Define.FAILED_PROCESSING,
                    HttpStatus.INTERNAL_SERVER_ERROR); // INTERNAL_SERVER_ERROR (= 500 내부 서버 오류)
        }
    } // end of updateAccountDeposit
}
