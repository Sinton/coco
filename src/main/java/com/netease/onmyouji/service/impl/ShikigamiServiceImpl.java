package com.netease.onmyouji.service.impl;

import com.netease.onmyouji.entity.Shikigami;
import com.netease.onmyouji.mapper.ShikigamiMapper;
import com.netease.onmyouji.service.ShikigamiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ShikigamiServiceImpl implements ShikigamiService {

    @Autowired
    private ShikigamiMapper shikigamiMapper;

    @Override
    public void addShikigami(Shikigami shikigami) {
        shikigamiMapper.insertShikigami(shikigami);
    }

    @Override
    public void removeShikigami(Shikigami shikigami) {
        shikigamiMapper.deleteShikigami(shikigami);
    }

    @Override
    public void modifyShikigami(Shikigami shikigami) {
        shikigamiMapper.updateShikigami(shikigami);
    }

    @Override
    public List<Shikigami> getAllShikigamis() {
        return shikigamiMapper.selectAllShikigamis();
    }

    @Override
    public Shikigami getShikigamiById(int identifier) {
        return shikigamiMapper.selectShikigamiByIdentifier(identifier);
    }
}