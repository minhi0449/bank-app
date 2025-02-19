package com.tenco.bank.service;

import com.tenco.bank.dto.*;
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

import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.AccountRepository;

import java.util.ArrayList;
import java.util.List;

/*
  날짜 : 2025.02.13 (목)
  이름 : 김민희
  내용 : 계좌 생성을 위한 AccountService 설계

  이력 : 2025.02.14 (금) - 김민희 : 입/출금 기능 추가
        2025.02.17 (월) - 김민희 : 단일 계좌 조회 기능 추가
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
        // System.out.println("111111 : " + rowResetCount);
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
        // ** 비밀번호 필요  **

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



    /**
     * 이체 기능 처리
     * @param dto
     * @param principalId
     */
    // 1. 트랜잭션 처리
    // 2. 출금 계좌 존재 여부 확인 --> select
    // 3. 출금 계좌 본인 소유 여부 확인 --> 객체 상태 값 에서 확인할 수 있고,
    // -. 입금 계좌 존재 여부 확인 --> select (헷갈려서 아래로 내려감)
    // 4. 출금 계좌 비밀번호 확인 --> 객체 상태 값
    // 5. 출금 잔액 여부 확인 --> 객체 상태 값 (잔액 보다 초과된 금액 이체 불가)
    // 6. 입금 계좌 존재 여부 확인 --> select
    // 7. 출금 계좌 잔액 수정 --> 객체 상태 값
    // 8. 출금 계좌 잔액 수정 --> update 쿼리문 때려야 함 -> 아야,,
    // 9. 입금 계좌 객체 상태 변경 --> 객체 상태 값
    // 10. 입금 계좌 잔액 변경 --> update
    // 11. 거래 내역 등록 처리
    @Transactional
    public void updateAccountTransfer(TransferDTO dto, Integer principalId) {
//        // 1. 출금 계좌 존재 여부
//        Account account = accountRepository.findByNumber(dto.getWAccountNumber());
//        // 1.2 계좌 번호가 없는 상황이라면 예외던지기
//        if(account == null){
//            throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
//        }
//        // 2. 출금 계좌 본인 소유 여부 확인
//        account.checkOwner(principalId);
//        // 3. 출금 계좌 비밀번호 확인
//        account.checkPassword(dto.getPassword());
//        // 4. 출금 잔액 여부 확인
//        account.checkBalance(dto.getAmount());
//        // 5. 입금 계좌 존재 여부 확인
//        //account.checkOwner(dto.getDAccountNumber());
        ///////////////////////////////////////////////////////////////////


        Account withdrawAccount = accountRepository.findByNumber(dto.getWAccountNumber());
        // 2. 출금 계좌 존재 여부 확인 --> select
        if (withdrawAccount == null){
            throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
        }
        // 3. 출금 계좌 본인 소유 여부 확인 --> 객체 상태 값 에서 확인할 수 있고,
        withdrawAccount.checkOwner(principalId);

        // 4. 출금 계좌 비밀번호 확인 --> 객체 상태 값
        withdrawAccount.checkPassword(dto.getPassword());

        // 5. 출금 잔액 여부 확인 --> 객체 상태 값 (잔액 보다 초과된 금액 이체 불가)
        withdrawAccount.checkBalance(dto.getAmount());

        // 6. 입금 계좌 존재 여부 확인 --> select
        Account depositAccount = accountRepository.findByNumber(dto.getDAccountNumber());
        if(depositAccount == null){
            throw new DataDeliveryException("상대방의 계좌 번호가 없습니다.", HttpStatus.BAD_REQUEST);
        }

        // 7. 출금 계좌 잔액 수정 --> 객체 상태 값 변경
        withdrawAccount.withdraw(dto.getAmount());

        // 8. 출금 계좌 잔액 수정 --> update 쿼리문 때려야 함 -> 아야,,
        accountRepository.updateById(withdrawAccount);

        // 9. 입금 계좌 객체 상태 변경 --> 객체 상태 값
        depositAccount.deposit(dto.getAmount());

        // 10. 입금 계좌 잔액 변경 --> update
        accountRepository.updateById(depositAccount); // depositAccount 상태값을 넣으면 상태 값 변경 됨

        // 11. 거래 내역 등록 처리
        // 전체 내역이 다 들어가야 하기 때문에 History 객체 만들어야 함
        History history = History.builder()
                .amount(dto.getAmount()) // 이체 금액
                .wAccountId(withdrawAccount.getId()) // 출금 계좌 PK
                .dAccountId(depositAccount.getId()) // 입금 계좌 PK
                .wBalance(withdrawAccount.getBalance()) // 출금 계죄 잔액 (그 시점)
                .dBalance(depositAccount.getBalance()) // 입금 계좌 잔액 (그 시점)
                .build();

        int resultRowCount = historyRepository.insert(history);

        if(resultRowCount != 1){
            throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    // 단일 계좌 조회 기능

    /**
     * 단일 계좌 조회 기능
     * select 부분은 트랜잭션 안 걸기로 함
     * @param accountId
     * @return
     */
    public Account readAccountId(Integer accountId){
        log.info("🧤단일 계좌 조회 기능 + readAccountId()");
        Account account = accountRepository.findByAccountId(accountId);
        if(accountId == null){
            throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
        }
        return account;
    }

    /**
     * 단일 계좌 거래 내역 조회
     * @param type [all, deposit, withdraw]
     * @param accountId (pk)
     * @return 입금, 출금, 입출금 거래내역 (3가지 타입으로 반환 처리)
     */
    public List<HistoryAccountDTO> readHistoryByAccountId(
            String type, Integer accountId, int page, int size) {
        log.info("⁉️ 안녕 여기는 서비스 + readHistoryByAccountId() ");
        // controller 에서 page, size 받을 것임
        // limit 변수 , offset 변수
        int limit = size;
        // 3번째 페이지를 보여주기
        // limit 2, offset ??
        // 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        int offset = (page - 1) * size; // 넘겨받았던 사이즈를 곱하기?

        // 현재 페이지 1이라고 가정을 하면
        // offset 는 0번부터 시작하니까
        // (현재 페이지 1 * size)

        // 지역 변수 선언
        List<HistoryAccountDTO> list = new ArrayList<>();// 자료 구조 부터 만들게욧@!
        // 1단계 limit , offset 몇 개까지 나와라, 어디서 부터 어디까지
        list = historyRepository.findByAccountIdAndTypeOfHistory(type, accountId, limit, offset); // 페이징 처리 없이 전체 페이지 들고 오는 거
        log.info("단일 계좌 거래 내역 조회 list : " + list);

        return list;
    }

    /**
     * 페이징 처리 하는 메서드
     * 단일 해당 계좌와 거래 유형에 따른 전체 레코드 수를 반환하는 메서드
     * @param type
     * @param accountId
     * @return int
     */
    public int countHistoryByAccountAndType(String type, Integer accountId, int page, int size) {
        log.info("0️⃣ 페이징 처리 하는 메서드 countHistory~ ");

        return historyRepository.countHistoryAccountIdAndType(type, accountId, page, size);
    }

}
