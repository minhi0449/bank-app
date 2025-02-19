package com.tenco.bank.dto;

import com.tenco.bank.repository.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/*
  날짜 : 2025.02.13 (목)
  이름 : 김민희
  내용 : 회원가입 DTO

  이력 :
 */


// SignUpFormDTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpDTO {

    private String username;
    private String password;
    private String fullname;

//  ToDo 추후 진행 예정
	private MultipartFile customFile; // name 속성과 일치 시켜야 함
	private String originFileName;
	private String uploadFileName;
    // 똑같은 이름이 있다면 ? -> 덮어쓰기 됨
    // 데이터베이스에서는 사용자가 업로드한 파일을 관리해줄 거임

    // private String eMail;


    // DTO --> 변환
    public User toUser(){
        return User.builder()
                .username(this.username)
                .password(this.password)
                .fullname(this.fullname)
                .uploadFileName(this.uploadFileName)
                .originFileName(this.originFileName)
                .build();
    }



}