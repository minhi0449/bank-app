package com.tenco.bank.repository.model;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
    날짜 : 2025.02.12 (수)
    이름 : 김민희
    내용 :
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String username;
    private String password;
    private String fullname;
    private Timestamp createdAt;
    private String uploadFileName; // 프로필 이미지 이름
    private String originFileName;

    public String setUpUserImage(){
        return uploadFileName == null ?
                "https://picsum.photos/id/237/200/300"
                : "images/uploads/" + uploadFileName;
    }
}
