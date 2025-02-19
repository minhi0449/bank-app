package com.tenco.bank.repository.interfaces;

import com.tenco.bank.repository.model.User;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;


import java.util.List;

/**
 * 날짜 : 2025.02.13 (목)
 * 이름 : 김민희
 * 내용 :
 */

// 마이바티스를 만들기 위해서 인터페이스를 만들고 xml 파일을 정의한다
// 인터페이스 만들고  + xml 파일 정의한다.
@Mapper // 반드시 선언해 주어야 한다.
public interface UserRepository {
    public int insert(User user); // 메서드 이름이랑 xml id
    public int updateById(User user);
    public int deleteById(Integer id);
    public int findById(Integer id);
    public List<User> findAll();

    public User findByUsernameAndPassword(
            @Param("username") String username,
            @Param("password") String password);

    public User findByUsername(String username);
}
