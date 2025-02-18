package com.tenco.bank.controller;


import com.tenco.bank.dto.SignInDTO;
import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.service.UserService;
import com.tenco.bank.utils.Define;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Log4j2
@Controller // IoC 대상 --> 보통 HTML 렌더링(자바코드) ---> 클라이언트 응답
@RequiredArgsConstructor // 리콰이얼드 아그스 컨스트럭쳐
@RequestMapping("/user")
public class UserController {

    // @Autowired --> 가독성
    private final UserService userService;
    // 세션 메모리지에 접근하는 클래스가 있다.
    private final HttpSession session;

    /**
     * 회원가입
     * 주소 설계 : http://localhost:8080/user/sign-up
     */
    @GetMapping("/sign-up")
    public String signUpPage() {
        return "/user/signUp";
    }

    // 회원 가입 요청 처리
    // 주소 설계 http://localhost:8800/user/sign-up
    // Get, Post -> sign-up 같은 도메인이라도 구분이 가능하다.
    // REST API 를 사용하는 이유에 대해한번 더 살펴 보세요
    @PostMapping("/sign-up")
    public String signProc(SignUpDTO dto) {
        // 1. 인증검사 x
        // 2. 유효성 검사
        if(dto.getUsername() == null || dto.getUsername().isEmpty()) {
            throw new DataDeliveryException("username을 입력 하세요",
                    HttpStatus.BAD_REQUEST);
        }

        if(dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new DataDeliveryException("password을 입력 하세요",
                    HttpStatus.BAD_REQUEST);
        }

        if(dto.getFullname() == null || dto.getFullname().isEmpty()) {
            throw new DataDeliveryException("fullname을 입력 하세요",
                    HttpStatus.BAD_REQUEST);
        }
        userService.createUser(dto);
        // todo 로그인 페이지로 변경 예정
        // todo /account/list 경로로 변경해야 함
        return "redirect:/user/sign-in";
    }

    /**
     * 로그인 화면 요청
     * @return
     */
    @GetMapping("/sign-in")
    public String singInPage() {
        log.info("🍎로그인 ");
        return "/user/signIn";
    }


    /**
     * 예외적으로 보안상 이유로 POST을 사용한다.
     * -- 특정 --> GET 로그인 --> 암호화
     * 로그인 요청 처리
     * 주소설계 : http://localhost:8080/user/sign-in
     * @return
     */
    @PostMapping("/sign-in")
    public String signProc(SignInDTO dto) {

        // 유효성 검사
        if(dto.getUsername() == null || dto.getUsername().isEmpty()) {
            throw new DataDeliveryException(Define.ENTER_YOUR_USERNAME, HttpStatus.BAD_REQUEST);
        }
        if(dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
        }

        User principal = userService.readUser(dto);
        session.setAttribute(Define.PRINCIPAL, principal);

        // Define 에 공통적으로 사용되는 상수(Constant)들을 정의

        return "redirect:/account/list";
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/user/sign-in"; // redirect 는 주소를 부르는 건뎅.. 이 바보 멍청아
    }



}