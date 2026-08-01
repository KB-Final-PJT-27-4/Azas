package com.azas.domain.auth.mapper;

import com.azas.domain.auth.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RefreshTokenMapper {

    int insert(RefreshToken refreshToken);
}
