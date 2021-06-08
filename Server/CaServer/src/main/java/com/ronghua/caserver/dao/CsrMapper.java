package com.ronghua.caserver.dao;

import com.ronghua.caserver.entity.CsrEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CsrMapper {

    void insertCsr(CsrEntity entity);

    List<CsrEntity> getCsrsByNameAndCode(@Param("username")String username, @Param("code")String code);

    void deleteCsrByNameAndCode(@Param("username")String username, @Param("code")String code);
}
