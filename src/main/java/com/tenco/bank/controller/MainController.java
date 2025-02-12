package com.tenco.bank.controller;

import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// SSR 으로 랜더링 됨
@Controller  // IoC 대상(싱글톤 패턴 관리가 된다.) --> 제어의 역전  
public class MainController {



	// REST API  기반으로 주소설계 가능

	// 	// http:localhost:8080/main-page
	// 주소설계
	// http:localhost:8080/main-page
	@GetMapping({"/main-page", "/index", "/"})
	// @ResponseBody
	public String mainPage() {
	 // throw new DataDeliveryException("메시지가 몰라? ", HttpStatus.UNAUTHORIZED);
	 throw new RedirectException("메시지가 몰라? ", HttpStatus.UNAUTHORIZED);
		// System.out.println("🔴 mainPage() 호출 확인");
		// [JSP 파일 찾기 (yml 설정) ] - 뷰 리졸버 
		// prefix: /WEB-INF/view
		//         /main  
		// suffix: .jsp

		// /WEB-INF/view/main/.jsp
		// return "/main";
	}
	

}


