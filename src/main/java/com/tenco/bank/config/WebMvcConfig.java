package com.tenco.bank.config;

import com.tenco.bank.handler.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
    날짜 : 2025.02.18 (화)
    이름 : 김민희
    내용 : AuthInterceptor를 WebMvcConfig에 등록하여 특정 경로에 인터셉터 적용

 */

@Log4j2
@RequiredArgsConstructor
@Configuration // IoC 대상 (스프링 부트 설정 클래스 이다.)
public class WebMvcConfig implements WebMvcConfigurer {

    // 방금 만든 인터셉터 적용할 거임
    // + 인터셉터 추가로 만들 수 있음
    // 메모리에 떳으니까 가지고 올 수 있는 거 겠죠?
    // DI 처리
    private final AuthInterceptor authInterceptor; // 멤버변수로 선언


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/account/**") // 이거 아래에서 모두 동작해줘 bro
                .addPathPatterns("/auth/**");

    }

    // Bean 객체
    @Bean // IoC 대상
    PasswordEncoder passwordEncoder(){
        return  new BCryptPasswordEncoder();
    }


}

