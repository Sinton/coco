package com.netease.onmyouji.mapper;

import com.netease.onmyouji.entity.Shikigami;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShikigamiMapper {

    void insertShikigami(@Param("shikigami") Shikigami shikigami);

    void deleteShikigami(@Param("shikigami") Shikigami shikigami);

    void updateShikigami(@Param("shikigami") Shikigami shikigami);

    List<Shikigami> selectAllShikigamis();

    Shikigami selectShikigamiByIdentifier(@Param("identifier") int identifier);
}