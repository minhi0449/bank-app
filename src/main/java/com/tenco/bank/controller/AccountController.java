package com.tenco.bank.controller;

import com.tenco.bank.dto.AccountSaveDTO;
import com.tenco.bank.dto.DepositDTO;
import com.tenco.bank.dto.TransferDTO;
import com.tenco.bank.dto.WithdrawalDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.UnAuthorizedException;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.service.AccountService;
import com.tenco.bank.utils.Define;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HexFormat;
import java.util.List;

/**
 * 날짜 : 2025.02.13 (목)
 * 이름 : 김민희
 * 내용 :
 */

@RequestMapping("/account")
@Controller

public class AccountController {

    @Autowired
    private final HttpSession session;
    @Autowired
    private final AccountService accountService;

    // 생성자 의존 주입 - DI 처리
    public AccountController(HttpSession session, AccountService accountService) {
        this.session = session;
        this.accountService = accountService;
    }
    /**
     * 계좌 목록 화면 요청
     * 주소 : http://localhost:8080/account/list
     * @param model
     * @return list.jsp
     */
//    @GetMapping({"list", "/"})
//    public String listPage(Model model) {
//        return "/account/list";
//    }

    // /account/save.jsp
    @GetMapping("/save")
    public String savePage(){
        // 파라미터 받을 필요 없고, 리턴만 받아주면 됨

        // 값을 끌어내려면? -> getAttribute ();
        // 여기에 접근했을 때, null 이면
        // (User) session -> 이거는 형변환
        User principal = (User) session.getAttribute(Define.PRINCIPAL);

        if(principal == null) {
            throw  new UnAuthorizedException(
                    "인증된 사용자가 아닙니다." , HttpStatus.UNAUTHORIZED);
        }

        return "/account/save";
    }


    // 계좌 생성 기능
    @PostMapping("/save")
    public String saveProc(AccountSaveDTO dto) {
        // 유효성 검사보다 먼저 인증검사를 먼저 하는 것이 좋습니다.

        // 1. 인증검사
        User principal = (User)session.getAttribute("principal");
        if(principal == null) {
            throw new UnAuthorizedException("로그인 먼저 해주세요",
                    HttpStatus.UNAUTHORIZED);
        }

        // 2. 유효성 검사
        if(dto.getNumber() == null || dto.getNumber().isEmpty()) {
            throw new DataDeliveryException("계좌번호를 입력하시오",
                    HttpStatus.BAD_REQUEST);
        }

        if(dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new DataDeliveryException("계좌 비밀번호를 입력하시오",
                    HttpStatus.BAD_REQUEST);
        }

        if(dto.getBalance() == null || 	dto.getBalance() <= 0 ) {
            throw new DataDeliveryException("잘못된 입력 입니다",
                    HttpStatus.BAD_REQUEST);
        }
        accountService.createAccount(dto, principal.getId());

        // TODO 추후 account/list 페이지가 만들어 지면 수정 할 예정 입니다. -> 수정완료
        return "redirect:/account/list";
    }


    /**
     * 계좌 목록 페이지
     *
     * @param model - accountList
     * @return list.jsp
     */
    @GetMapping({ "/list", "/" })
    public String listPage(Model model) {

        // 1.인증 검사가 필요(account 전체 필요)
        User principal = (User) session.getAttribute("principal");
        if (principal == null) {
            throw new UnAuthorizedException("인증된 사용자가 아닙니다", HttpStatus.UNAUTHORIZED);
        }

        // 경우의 수 -> 유, 무
        List<Account> accountList = accountService.readAccountListByUserId(principal.getId());

        if (accountList.isEmpty()) {
            model.addAttribute("accountList", null);
        } else {
            model.addAttribute("accountList", accountList);
        }

        return "/account/list";
    }

    // 출금하기
    @GetMapping("/withdrawal")
    public String withdrawalPage(){

        User principal = (User) session.getAttribute(Define.PRINCIPAL);
        // 매번 작성하기 좀 그러니까 인증 처리를 어디서 진행했는지 여쭤보셨음
        // filter 에서 진행해도 되고, 인터셉터에서 진행해도 됨
        if(principal == null){
            throw new UnAuthorizedException(Define.ENTER_YOUR_PASSWORD, HttpStatus.UNAUTHORIZED);
        }

        return "/account/withdrawal";
    }

    //
    @PostMapping("/withdrawal")
    public String withdrawalProc(WithdrawalDTO dto) {

        User principal = (User) session.getAttribute(Define.PRINCIPAL);
        if(principal == null) {
            throw new UnAuthorizedException(Define.ENTER_YOUR_LOGIN, HttpStatus.UNAUTHORIZED);
        }

        // 2. 유효성 검사
        // 유효성 검사
        if(dto.getAmount() == null) {
            throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE,
                    HttpStatus.BAD_REQUEST);
        }

        if(dto.getAmount() <= 0) {
            throw new DataDeliveryException(Define.W_BALANCE_VALUE,
                    HttpStatus.BAD_REQUEST);
        }

        if(dto.getWAccountNumber() == null) {
            throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER,
                    HttpStatus.BAD_REQUEST);
        }

        if(dto.getWAccountPassword() == null || dto.getWAccountPassword().isEmpty() ) {
            throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD,
                    HttpStatus.BAD_REQUEST);
        }

        accountService.updateAccountWithdraw(dto, principal.getId());

        return "redirect:/account/list";
    }


    @GetMapping("/deposit")
    public String depositPage(){
        // 1. 인증 검사
        User principal = (User) session.getAttribute(Define.PRINCIPAL); // 다운 캐스팅
        if (principal == null) {
            throw new UnAuthorizedException(Define.ENTER_YOUR_LOGIN, HttpStatus.UNAUTHORIZED);
        }
        return "/account/deposit";
    }

    /**
     * 입금 기능 처리
     * @param DepositDTO
     * @return 계좌 목록 페이지
     */
    @PostMapping("/deposit")
    public String depositProc(DepositDTO dto) { // form 태그에서 왔던 거 여기서 받는 거고
        // 1. 인증 검사
        User principal = (User) session.getAttribute(Define.PRINCIPAL); // 다운 캐스팅
        if (principal == null) {
            throw new UnAuthorizedException(Define.ENTER_YOUR_LOGIN, HttpStatus.UNAUTHORIZED);
        }

        // 2. 유효성 검사
        if (dto.getAmount() == null) {
            throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
        }
        if (dto.getAmount().longValue() <= 0) {
            throw new DataDeliveryException(Define.D_BALANCE_VALUE, HttpStatus.BAD_REQUEST);
        }
        if (dto.getDAccountNumber() == null) {
            throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
        }

        // 서비스 호출
        accountService.updateAccountDeposit(dto, principal.getId());

        return "redirect:/account/list";

    }



    // 이체 기능 화면 요청
    @GetMapping("/transfer")
    public String transgerPage(){
        // 인증 검사
        User principal = (User) session.getAttribute(Define.PRINCIPAL);
        // 테스트 할 때, 인증 검사를 해두지 않으면 바로 로그인 가능!
//        if(principal == null){ // null 이 아니라면 로그인 한 사용자
//            throw new UnAuthorizedException(Define.ENTER_YOUR_LOGIN,
//                    HttpStatus.UNAUTHORIZED);
//        }
        return "/account/transfer";
    }

    // 출금 기능 처리하는
    @PostMapping("/transfer")
    public String transferProc(TransferDTO dto){
        System.out.println("안녕 여기 이체하기 toString" + dto.toString());

        // 인증 검사
        User principal = (User) session.getAttribute(Define.PRINCIPAL);
         // 테스트 할 때, 인증 검사를 해두지 않으면 바로 로그인 가능!
        if(principal == null){ // null 이 아니라면 로그인 한 사용자
            throw new UnAuthorizedException(Define.ENTER_YOUR_LOGIN,
                    HttpStatus.UNAUTHORIZED);
        }


        // 유효성 검사
        if(dto.getAmount() == null){
            throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
        }
        if(dto.getAmount() < 0){ // 출금 금액 0일 떄,
            throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
        }

        // 3. 출금 계좌번호 입력 여부 확인
        if(dto.getWAccountNumber() == null || dto.getWAccountNumber().isEmpty()){
            throw new DataDeliveryException("출금 계좌 번호를 입력하시오", HttpStatus.BAD_REQUEST);
        }

        // 4. 출금 계좌 비밀번호 입력 여부 확인
        // 5. 입금 계좌 입력 여부 확인
        if(dto.getDAccountNumber() == null || dto.getDAccountNumber().isEmpty()){
            throw new DataDeliveryException("입금 계좌 번호를 입력하시오", HttpStatus.BAD_REQUEST);
        }
        // 6. 같은 계좌 입력인지 확인 (출금 = 입금 계좌 번호가 같으면 안되니까 !)
        // if(dto.getWAccountNumber() == dto.getDAccountNumber()){ 문자열 타입 인데 == 으로 비교해서 실수한 부분
        if(dto.getWAccountNumber().equals(dto.getDAccountNumber())){ // 문자열은 .equals 로 비교해야 함
            throw new DataDeliveryException("같은 계좌로 이체할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        // 서비스 호출
        accountService.updateAccountTransfer(dto, principal.getId());
        // 본인의 계좌에서 출금한 게 맞는지 알아야 하니까 -> 유저 아이디 들어가야 함

        return "redirect:/account/list";
    }


}