package com.tenco.bank.service;

import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.repository.interfaces.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // 리콰이얼드 아그스 컨스트럭쳐
public class UserService{
    // @Autowired
    // final : 불변객체 -> 한 번 객체로 사용되면
    // 이러한 멤버가 있다면 또 작성해야 함
    private final UserRepository userRepository;

    // 생성자 의존 주입 DI --> UserRepository 자동 주입
//    public UserService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }

    // 회원가입
    public int createUser(SignUpDTO dto){
        // 서비스에서 dto 만 신경쓰면 됨

        int result = 0;


        result = userRepository.insert(dto.toUser());
        // insert 에 User 라는 모델을 받아야 함
        // SignUpDTO 에 toUser 라는 거 만들어 놨음
        //

        return result;

    }



}