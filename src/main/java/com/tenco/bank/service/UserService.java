package com.tenco.bank.service;

import com.tenco.bank.dto.SignInDTO;
import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.UserRepository;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.utils.Define;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.apache.logging.log4j.ThreadContext.isEmpty;


// 2025.02.19 (수) - 비밀번호 암호화 코드 추가

@Service
@RequiredArgsConstructor // 리콰이얼드 아그스 컨스트럭쳐
public class UserService{
    // @Autowired
    // final : 불변객체 -> 한 번 객체로 사용되면
    // 이러한 멤버가 있다면 또 작성해야 함
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 초기 파라미터 들고 오는 방법
    @Value("${file.upload-dir}")
    private String uploadDir; // C:\\work_spring\\upload/  매핑 됨


    // 생성자 의존 주입 DI --> UserRepository 자동 주입
//    public UserService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }


    // 회원 가입 처리
    // 예외 처리
    // DB 에서 연결이나 쿼리 실행, 제약 사항 위한 같은
    // 예외는 RuntimeException 으로 예외를 잡을 수 없습니다.
    @Transactional // 트랜 잭션 처리 습관 --> 중간에 오류나면 rollback 시키는 거
    public void createUser(SignUpDTO dto) {
        // 서비스에서 dto 만 신경쓰면 됨
        // Http 응답으로 클라이언트에게 전달할 오류 메시지는 최소한으로 유지하고,
        // 보안 및 사용자 경험 측면에서 민감한 정보를 노출하지 않도록 합니다.
        int result = 0;

        // 회원가입 시 insert 처리 O/U
        System.out.println("▶️ getCustomFile() 파일 이름 들어왔나? "+dto.getCustomFile());
        if(dto.getCustomFile() != null && !dto.getCustomFile().isEmpty()){ // 파일이 있다면?
            // 업로드 로직 구현
            String[] fileNames = uploadFile(dto.getCustomFile());
            dto.setOriginFileName(fileNames[0]);
            dto.setUploadFileName(fileNames[1]);

            // 파일 업로드 로직 구현
            // System.out.println(dto.getCustomFile().getName());
            // System.out.println(dto.getCustomFile().getOriginalFilename()); // 파일의 진짜 이름? 으로 불러줘야 함
        }

        try {
            // 우리가 알 수 없는 비밀번호 암호화 해서 해주는 코드
            String hashPwd =  passwordEncoder.encode(dto.getPassword());
            dto.setPassword(hashPwd); // 상태 변경

            result = userRepository.insert(dto.toUser());
            // insert 에 User 라는 모델을 받아야 함
            // SignUpDTO 에 toUser 라는 거 만들어 놨음
            // 여기서 예외 처리를 하면 상위 catch 블록에서 예외를 잡는다.
        } catch (DataAccessException e) { // 데이터베이스가 꺼져있거나,
            // DataAccessException는 Spring의 데이터 액세스 예외 클래스로,
            // 데이터베이스 연결이나 쿼리 실행과 관련된 문제를 처리합니다.
            throw new DataDeliveryException("잘못된 처리 입니다", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // 그 외 예외 처리 - 페이지 이동 처리 RedirectException(리다이렉션 에러페이지 보는 페이지로 이동시키기)
            throw new RedirectException("알 수 없는 오류" , HttpStatus.SERVICE_UNAVAILABLE);
        }
        // 예외 클래스가 발생이 안되지만 프로세스 입장에서 예외 상황으로 바라 봄
        if (result != 1) {
            // 삽입된 행의 수가 1이 아닌 경우 예외 발생
            throw new DataDeliveryException("회원 가입 실패", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // session 에 User 등록하기
    public User readUser(SignInDTO dto) {
        // Repository Model 의 녀석을 땡겨오는 거임
        // 암호화 -> 복호화도 수업
        User user = null;
        try {
            // 한 번에 아이디, 비밀번호 넣고 있음


            // user = userRepository.findByUsernameAndPassword(dto.getUsername(), dto.getPassword());
            // 이름만 있는지 없는지 먼저 확인

            user = userRepository.findByUsername(dto.getUsername());


            // insert 에 User 라는 모델을 받아야 함
            // SignUpDTO 에 toUser 라는 거 만들어 놨음
            // 여기서 예외 처리를 하면 상위 catch 블록에서 예외를 잡는다.
        } catch (DataAccessException e) { // 데이터베이스가 꺼져있거나,
            // DataAccessException는 Spring의 데이터 액세스 예외 클래스로,
            // 데이터베이스 연결이나 쿼리 실행과 관련된 문제를 처리합니다.
            throw new DataDeliveryException("잘못된 처리 입니다", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // 그 외 예외 처리 - 페이지 이동 처리 RedirectException(리다이렉션 에러페이지 보는 페이지로 이동시키기)
            throw new RedirectException("알 수 없는 오류" , HttpStatus.SERVICE_UNAVAILABLE);
        }

        // optional 사용해도 되고, 코드 일관성있게만 작성하기만 하면 됨
        if (user == null) {
            throw new DataDeliveryException("아이디 또는 비밀번호가 맞지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        // 12345 ==
        boolean isPwdMatched = passwordEncoder.matches(dto.getPassword(), user.getPassword());

        if(isPwdMatched == false){
            throw new DataDeliveryException("비밀번호가 잘못되었습니다.", HttpStatus.BAD_REQUEST);
        }


        return user;
    }

    private  String[] uploadFile(MultipartFile mFile){
        // 방어적 코드 작성
        if(mFile.getSize() > Define.MAX_FILE_SIZE){
            throw new DataDeliveryException("파일 크기는 20MB 이상 클 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
        // 파일을 만들기 위해서
        // 1. 파일 경로를 설정해 준다.
        // String saveDirectory = uploadDir; 윈도우
        String saveDirectory = new File(uploadDir).getAbsolutePath();

        // 폴더 존재 여부 코드를 작성해 보자.
        File uploadFolder = new File(saveDirectory);
        System.out.println("🆙 폴더 존재 여부 + uploadFolder"+ uploadFolder);

        if(!uploadFolder.exists()){ // 만약에 존재하지 않는다면?
            boolean mkdirsResult = uploadFolder.mkdir();
            if(!mkdirsResult){
                throw new DataDeliveryException("파일 업로드 폴더를 생성할 수 없습니다.",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        // 리눅스, MacOS 맞춰서 절대 경로를 맞춰야 한다.


        // 2. 파일명이 중복되면 덮어쓰기 됩니다. 예방
        // 2.1 파일 이름을 생성한다. (가능한 절대 중복되지 않을 이름으로 생성)  + 나중에 추출해서 쓸 수 있도록 구분자 값 "_" 추가
        // 2.1 = adfsdfafsd1011_a.png
        String uploadFileName = UUID.randomUUID() + "_" + mFile.getOriginalFilename();
        // String uploadPath = saveDirectory + / 파일명을 적어 줄건데
        // 3. 파일명을 포함한 전체 경로를 만들자. --> 파일 전체 경로 + 새로 생성한 파일명 --> 문자열로 만들어짐
        String uploadPath = saveDirectory + File.separator + uploadFileName;
        // TODO : 파일 경로 console 출력 x
        System.out.println("✳️ uploadPath" + uploadPath);

        File destination = new File(uploadPath);
        System.out.println("ℹ️ destination"+ destination);

        try {

            mFile.transferTo(destination); // 컴파일 시점 오류 발생할 수 있음
        }catch (IOException e){
            e.printStackTrace();
            throw new DataDeliveryException("파일 업로드 중 오류가 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 파일 까지 생성 --> 원본 사진 명, 새로 생성한 파일명
        return new String[] {mFile.getOriginalFilename(), uploadFileName} ;
    }


}