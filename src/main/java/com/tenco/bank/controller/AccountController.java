package com.tenco.bank.controller;

import com.tenco.bank.handler.exception.UnAuthorizedException;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.utils.Define;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 날짜 : 2025.02.13 (목)
 * 이름 : 김민희
 * 내용 :
 */

@RequestMapping("/account")
@Controller
@RequiredArgsConstructor
public class AccountController {

    // DI 처리
    private final HttpSession session;

    /**
     * 계좌 목록 화면 요청
     * 주소 : http://localhost:8080/account/list
     * @param model
     * @return list.jsp
     */
    @GetMapping({"list", "/"})
    public String listPage(Model model) {
        return "/account/list";
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

}