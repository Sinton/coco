package com.netease.onmyouji.entity;

import java.io.Serializable;

public class Shikigami implements Serializable {

    private int identifier;
    private String name;
    private int rarity;

    public Shikigami() {
    }

    public Shikigami(int identifier, String name, int rarity) {
        this.identifier = identifier;
        this.name = name;
        this.rarity = rarity;
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRarity() {
        return rarity;
    }

    public void setRarity(int rarity) {
        this.rarity = rarity;
    }
}