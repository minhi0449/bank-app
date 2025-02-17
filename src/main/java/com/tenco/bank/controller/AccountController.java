package com.tenco.bank.controller;

import com.tenco.bank.dto.*;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.UnAuthorizedException;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.service.AccountService;
import com.tenco.bank.utils.Define;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 날짜 : 2025.02.13 (목)
 * 이름 : 김민희
 * 내용 :
 *
 * 이력 : 2025.02.18 (화) - 페이징 처리
 */

@RequestMapping("/account")
@Controller
@Log4j2
public class AccountController {

    // @Autowired
    private final HttpSession session;
    // @Autowired
    private final AccountService accountService;

    // 생성자 의존 주입 - DI 처리
    public AccountController(HttpSession session, AccountService accountService) {
        this.session = session;
        this.accountService = accountService;
    }


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
    public String saveProc(AccountSaveDTO dto, @SessionAttribute(Define.PRINCIPAL) User principal) {

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
    @GetMapping({ "/list"})
    public String listPage(Model model, @SessionAttribute("principal") User principal) {
        log.info("🆗 여기가 계좌 목록 페이지 - listPage()");

        // 경우의 수 -> 유, 무
        List<Account> accountList = accountService.readAccountListByUserId(1);

        if (accountList.isEmpty()) {
            model.addAttribute("accountList", null);
        } else {
            model.addAttribute("accountList", accountList);
        }

        return "/account/list";
    }

    // 출금하기
    @GetMapping("/withdrawal")
    public String withdrawalPage(@SessionAttribute(Define.PRINCIPAL) User principal){
        log.info("여기는 출금하기 페이지 컨트롤러 오바 + withdrawalPage() ");
        // 매번 작성하기 좀 그러니까 인증 처리를 어디서 진행했는지 여쭤보셨음
        // filter 에서 진행해도 되고, 인터셉터에서 진행해도 됨
        if(principal == null){
            throw new UnAuthorizedException(Define.ENTER_YOUR_PASSWORD, HttpStatus.UNAUTHORIZED);
        }

        return "/account/withdrawal";
    }

    //
    @PostMapping("/withdrawal")
    public String withdrawalProc(WithdrawalDTO dto, @SessionAttribute(Define.PRINCIPAL) User principal) {
        log.info("☢️ 호호 withdrawalProc() 컨트롤러 ");

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

        return "/account/deposit";
    }

    /**
     * 입금 기능 처리
     * @param
     * @return 계좌 목록 페이지
     */
    @PostMapping("/deposit")
    public String depositProc(DepositDTO dto, @SessionAttribute(Define.PRINCIPAL) User principal) { // form 태그에서 왔던 거 여기서 받는 거고
        // 1. @SessionAttribute(Define.PRINCIPAL) User principal -> 이녀석이 다운 캐스팅해서 꽂아줍니다.
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
        return "/account/transfer";
    }

    // 출금 기능 처리하는
    @PostMapping("/transfer")
    public String transferProc(TransferDTO dto, @SessionAttribute(Define.PRINCIPAL) User principal){
        System.out.println("💳 안녕 여기 이체하기 toString" + dto.toString());


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


    // 계좌 상세보기 화면 요청

    /**
     * 계좌 상세보기 화면
     * 주소 설계 : http://localhost:8080/account/detail/1
     * type 설계 : http://localhost:8080/account/detail/1?type=all, deposit, withdraw
     * @return
     */
    @GetMapping("/detail/{accountId}")
    public String detailPage(@PathVariable(name = "accountId") Integer accountId,
                            @RequestParam(required = false, name = "type") String type,
                             @RequestParam(name="page", defaultValue = "1") int page,
                             @RequestParam(name="size", defaultValue = "1") int size, // 한 페이지에 몇 개씩 보고자 하는 거
                             Model model){
        System.out.println("💰안녕 여기 - 계좌 상세 컨트롤러 : detailPage()");

        System.out.println("✳️ 자 인증검사 통과 했고, type :"+ type);



        // 유효성 검사
        List<String> vaildTypes = Arrays.asList("all", "deposit", "withdrawal");
        log.info("🅰️ 타입 확인 - vaildTypes() "+ vaildTypes);
        if(!vaildTypes.contains(type)){
            throw new DataDeliveryException("유효하지 않은 접근입니다.", HttpStatus.BAD_REQUEST);
        } // 의도하지 않은 url 접근 시 -> 접근 못 하게 하는 유효성 검사
        // 페이지 처리를 하기 위한 데이터
        // 전체 레코드 수가 필요하다. 히스토리 이력이 10
        // 한 페이지당 보여줄 객수는 1라고 가정 한다면
        // 10개 페이지가 생성된다. ---> 5페이지(2개씩 보여줄 경우) [3 3 3 1 (만약 3개씩 보여줄 경우)]
        // 전체 레코드 수를 가져와야 하고,
        // 토탈 페이지 수를 계산 해야 한다.
        int totalRecords = accountService.countHistoryByAccountAndType(type, accountId, page, size);
        log.info("ℹ️ totalRecords 전체 레코드 수 계산 " + totalRecords);

        // 설정에 맞는 전체 페이지 수를 계산해야 한다.
        //int totalPages = 0; // paging 을 계산하는 수식이 들어옴

        int totalPages = (int) Math.ceil((double) totalRecords/size);

        log.info("🅿️ totalPages 전체 페이지 수 계산 " + totalPages);
        // 화면을 구성하기 위한 필요한 데이터
        // 소유자 이름 -- account_tb (사용자 하나의 계좌번호가 필요)
        // 해당 계좌 번호 -- account_tb
        // 거래 내역 추출 -- history_tb
        Account account = accountService.readAccountId(accountId);
        log.info("🆙 account "+ account);
        // 동적 쿼리를 위해 type 을 먼저 만들어주고, accountId 를 던져 줄거임
        // readHistoryByAccountId(type);
        // 데이터 타입 역시나 List
        List<HistoryAccountDTO> historyList = accountService.readHistoryByAccountId(type, accountId, page, size);
        log.info(" ❗️historyList {}",historyList);

        // view resolve (뷰 리졸브) --> jsp 데이터를 내려줄 때,
        // Model
        model.addAttribute("account", account);
        System.out.println("1️⃣ account ::"+account);
        model.addAttribute("historyList", historyList);
        System.out.println("2️⃣ historyList ::"+historyList);
        model.addAttribute("currentPage", page);
        System.out.println("3️⃣ currentPage ::"+page);
        model.addAttribute("totalPages", totalPages);
        System.out.println("4️⃣ totalPages ::"+totalPages);
        // 이거 안 내려주면 2페이지 안 넘어감
        model.addAttribute("type", type);
        System.out.println("️5️⃣ type ::"+type);
        model.addAttribute("size", size);
        System.out.println("6️⃣ size ::"+size);



        return "/account/detail";
    }


}