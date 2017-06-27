package com.netease.onmyouji.service;

import com.netease.onmyouji.entity.Shikigami;

import java.util.List;

public interface ShikigamiService {

    void addShikigami(Shikigami shikigami);

    void removeShikigami(Shikigami shikigami);

    void modifyShikigami(Shikigami shikigami);

    List<Shikigami> getAllShikigamis();

    Shikigami getShikigamiById(int shikigamiById);
}