package com.tenco.bank.handler;

import com.tenco.bank.handler.exception.UnAuthorizedException;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.utils.Define;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

// Ioc
@Component
@Log4j2
public class AuthInterceptor implements HandlerInterceptor {
    // interface 안에는 추상메서드만 사용가능
    // default 메서드도 사용가능하게 함
    // 굳이 사용하지 않을 추상메서드를 사용할 필요가 없어서
    // defalut -> 구현 메서드
    // 앞단에서 오버라이드 할 필요 없어짐

    // 동작 흐름
    // 컨트롤러에 들어가기 전에 동작하는 메서드 이다.
    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("사실 여기는 preHandle()");
        // response 를 빈 껍데기로 미리 보내는 거임
        // 리퀘스트 헤더, 바디에서 세션 만들어질 수 있음
        // code 추가
        HttpSession session = request.getSession(); // 세션이라는 정보가 있는지 없는지 확인
        log.info(" 인터셉터 session 세션 세션 "+ session);
        // 인증 검사
        User principal = (User) session.getAttribute(Define.PRINCIPAL);
        // 테스트 할 때, 인증 검사를 해두지 않으면 바로 로그인 가능!
        if(principal == null){ // null 이 아니라면 로그인 한 사용자
            throw new UnAuthorizedException(Define.ENTER_YOUR_LOGIN,
                    HttpStatus.UNAUTHORIZED);
        }
        log.info("여기는 인터셉터 ▶️ 인증검사 통과");
        // return HandlerInterceptor.super.preHandle(request,response,handler);
        // IP 차단 하는 기능들 (만들 순 있지만) -> 보통 필터에서 함
        return true;
    }

    // 뷰가 렌더링 되지 전에 호출 시킬 수 있다.
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    // 요청 처리가 완료 된 후, 뷰 렌더링이 완료된 후 호출 된다.
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }


}
