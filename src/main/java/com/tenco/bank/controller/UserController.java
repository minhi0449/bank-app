package com.tenco.bank.controller;


import com.tenco.bank.dto.KakaoProfile;
import com.tenco.bank.dto.OAuthToken;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Log4j2
@Controller // IoC 대상 --> 보통 HTML 렌더링(자바코드) ---> 클라이언트 응답
@RequiredArgsConstructor // 리콰이얼드 아그스 컨스트럭쳐
@RequestMapping("/user")
public class UserController {

    // @Autowired --> 가독성
    private final UserService userService;
    // 세션 메모리지에 접근하는 클래스가 있다.
    private final HttpSession session;

    @Value("${tenco.key}")
    private String tencoKey;


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



    // 카카오 로그인
    @GetMapping("/kakao")
    public String kakaoLogin(@RequestParam(name = "code") String code){
        // 1. 인증 코드 출력 (디버깅 용)
        System.out.println("📍 여기 카카오 로그인 - 인가 code :"+ code);

        // 2. 카카오 서버로 부터 액세스 토큰 받기
        // - 카카오에서 발급한 인가 코드를 액세스 토큰과 교환하기 위한 요청을 보내야 한다.


        // HTTP 통신 요청하기 위해서 RestTemplate 객체를 생성해서 외부 API와 통신할 예정
        RestTemplate tokenRequestRestTemplate = new RestTemplate();

        // exchange - get, post, put, delete ..
        // 2.1 요청 헤더를 구성 : 전송 타입은 form-data 로 전송해야 한다.
        HttpHeaders tokenRequestHeaders = new HttpHeaders();
        tokenRequestHeaders.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        // 2.2 요청 바디를 구성 : grant_type, client_id, redirent_uri, code
        // Map<k, List<String>> --> {a: "1", b: "2"}, a: "", "" 맵구조에 키 값을 여러 개 가지고 싶다면?
        // MultiValueMap --> 내부적 Map<k, List<String>> 과 같다.
        // LinkedMultiValueMap --> 키의 삽입 순서를 유지합니다.
        // 이 순서 보장은 인덱스 연산자로 값을 꺼내는 편의성을 제공하는 객체 입니다.
        // 내부적으로 get 0번째, get(0) // value()는 기억이 가물 가물 핫김
        MultiValueMap<String, String> tokenRequestParams = new LinkedMultiValueMap<>();
        // 인터페이스로 선언하면 -> 쓰는 방법은 표준을 맞췄기 때ㅔ문에 -> 자료구조를 쉽게 쉽게 사용할 수 있었음
        // 순서를 만들어주는 Map 객체를 선언 -> Linked~ (); ->
        // 이더 레이터
        // tokenRequestRequestTemplate.exchange()

        tokenRequestParams.add("grant_type", "authorization_code");
        tokenRequestParams.add("client_id", "ed3b297823c2dd6240c841faef613b04");
        tokenRequestParams.add("redirect_uri", "http://localhost:8080/user/kakao");
        tokenRequestParams.add("code", code);

        // 2.3  헤더와 바디를 결합한 HttpEntity 생성
        // HTTP 요청 메시지를 구성하기 위해 헤더와 바디에 파라미터를 함께 구성해주는 객체 이다.
        // 즉, 이 객체를 통해서 API 요청 시 필요한 모든 정보를 한 번에 전달 할 수 있다.
        HttpEntity<MultiValueMap<String, String>> tokenRequestEntity
                = new HttpEntity<>(tokenRequestParams, tokenRequestHeaders);


        ResponseEntity<OAuthToken> tokenRespone =  tokenRequestRestTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                // 객체 주입
                HttpMethod.POST,
                tokenRequestEntity,
                OAuthToken.class
        );

        System.out.println("🍟 어이 토큰이 들어왔는가 -? tokenRespone : "+ tokenRespone.getBody().getAccessToken());

        // 3. 사용자 정보 API 요청을 해야 한다.
        // https://kapi.kakao.com/v2/user/me
        //
        RestTemplate profileRequestRestTemplate = new RestTemplate();
        HttpHeaders profileRequestHeaders = new HttpHeaders();
        // Bearer + 공백 한 칸 필수 !!
        profileRequestHeaders.add("Authorization",
               "Bearer " + tokenRespone.getBody().getAccessToken());

        profileRequestHeaders.add("Content-Type",
                "application/x-www-form-urlencoded;charset=utf-8");

        // 3.2 요청 바디 구성 (x)
        // 사용자 정보 요청은 별도의 바디 없이 헤더만 전달하면 됨 (문서)
        // 와일드 카드 쓸게여 <?> -> 제네릭 타입
        HttpEntity<?> profileRequestEntity = new HttpEntity<>(profileRequestHeaders);

        // 3.3 POST, GET API --> exchange() 메서드 사용
        ResponseEntity<KakaoProfile> profileResponse = profileRequestRestTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                profileRequestEntity,
                KakaoProfile.class
        );

        // 디버깅 용 (사용자 정보 확인)
        System.out.println("🍔 카카오 사용자 프로필(리스폰스.겟바디.겟프로퍼티스.겟닉네임) : "
                + profileResponse.getBody().getProperties().getNickname());


        // DTO 만들어야 한다.
        // 카카오로부터 받은 사용자 정보 객체
        KakaoProfile kakaoProfile = profileResponse.getBody(); // 객체가 들어가겠죠? 주소값이?
        System.out.println("🥤 카카오 프로필 toString() : "+kakaoProfile.toString());
        if (kakaoProfile == null || kakaoProfile.getProperties() == null) {
            throw new RuntimeException("카카오 사용자 정보를 가져오는데 실패했습니다.");
        }

        // 1. 최초 사용자라면 자동 회원 가입 처리 (우리 서버) --> 세션 등록
        /// (필요 없으므로 진행 x)만약 회원 가입 시에 필수적으로 사용자 정보가 필요하다면 --> 화면 생성 (추가 정보 받아야 한다)
        // 2. 회원가입 이력이 있는 사용자라면 바로 세션에 등록 처리 해야 한다. --> 세션 등록

        // User user = userService.readUser();

        // 회원가입 정보를 담은 DTO 생성
        SignUpDTO signUpDTO = SignUpDTO
                .builder()
                // 기존 회원과 , 소셜 로그인 네임이
                .username("OAuth_" + kakaoProfile.getProperties().getNickname())
                .fullname("OAuth_" + kakaoProfile.getProperties().getNickname())
                .password(tencoKey) // 미리 정의된 더미 비밀번호
                .build();

        User user = userService.searchUsername(signUpDTO.getUsername());
        // ⭐️ 문제의 원인
        // --> 최초 소셜 로그인 시에 회원 가입까지 정상 동작
        // --> 자동 로그인 처리 (세션에 유저 정보 등록 실패 --> 다시 로그인 하라)
        // --> 최초 접근 시라도 자동 로그인 처리 해야 한다.
        if (user == null){
            // 한 번도 온 적이 없으니까
            userService.createUser(signUpDTO); // 회원가입 처리
            user = userService.searchUsername(signUpDTO.getUsername()); // 다시 조회하면? 이제 signDTO 에 들어갔으니까 조회되겠지
        }

        // 세션에 정보 저장
        session.setAttribute(Define.PRINCIPAL,user); // 방금 뽑아줬던 user 를 뽑아줘야 한다.
        // session 정보를 저장했는데 왜 로그인을 다시 하라고 했을까?

        // 소셜 로그인 , 회원가입 한 시점에 세션 정보를 저장 -> 해야 하고 , if(user == null) 처리를 앞에서 처리

        // 서비스 호출해서 사용자 (username)
        // 있으면 최초 사용자 x, 없으면 최초 사용자 --> insert 처리 하기
        return "redirect:/account/list";
    }

}