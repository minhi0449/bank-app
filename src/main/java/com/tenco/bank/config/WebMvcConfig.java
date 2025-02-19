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
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
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

        log.info("⏹️ 여기 패스워드 인코더");

        return  new BCryptPasswordEncoder();
    }

    // 프로젝트에 사용할 가상 경로 정의 -> 페이지 소스 보기(가상 경로) (/immages/uploads) // code 상에서 보여지는 건 이거지만
    // 실제 파일의 경로 file: // C: // spring_work // upload\ // 실제 컴퓨터에서 저장되는 위치는 이거 임
    // file:///Users/kimminimanimu/upload/57bf33bb-76a0-4716-be62-a4a79b998ea8_kkobugi.jpg
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("⏏️ 여기 업로드 파일 경로 ");
        registry.addResourceHandler("/images/uploads/**")
                .addResourceLocations("file:///Users/kimminimanimu/upload/");
    }
}

