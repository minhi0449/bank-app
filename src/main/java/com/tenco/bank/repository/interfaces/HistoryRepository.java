package com.tenco.bank.repository.interfaces;

import java.util.List;

import com.tenco.bank.dto.HistoryAccountDTO;
import org.apache.ibatis.annotations.Mapper;
import com.tenco.bank.repository.model.History;
import org.apache.ibatis.annotations.Param;

/*
  날짜 : 2025.02.13 (목)
  이름 : 김민희
  내용 :
 */

// 거래내역 관리

@Mapper
public interface HistoryRepository {

    public int insert(History history);
    public int updateById(History history);
    public int deleteById(Integer id);

    // 계좌 조회
    public History findById(Integer id);
    public List<History> findAll();

    // 단일 계좌 거래 내역 조회 (동적 쿼리 사용)
    public List<HistoryAccountDTO>
    findByAccountIdAndTypeOfHistory(
            @Param("type") String type,
            @Param("accountId") Integer accountId,
            @Param("limit") int limit,
            @Param("offset") int offset);

    // Map, 객체, 원시 타입으로 선언하는 방법이 있다.
    public int countHistoryAccountIdAndType(@Param("type") String type,
                                            @Param("accountId") Integer accountId,
                                            @Param("limit") int limit,
                                            @Param("offset") int offset);

}