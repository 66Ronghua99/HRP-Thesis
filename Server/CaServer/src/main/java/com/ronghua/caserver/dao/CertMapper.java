package com.ronghua.caserver.dao;

import com.ronghua.caserver.entity.CertEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CertMapper {

    void insertCert(CertEntity entity);

    CertEntity getCertByName(String username);

    void deleteCertByName(String username);

    void deleteAllInvalid(long time);

}
