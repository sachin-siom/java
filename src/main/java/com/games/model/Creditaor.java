package com.games.model;

import lombok.Getter;

@Getter
public enum Creditaor {
    USER(1),
    ADMIN(2);

    Creditaor(int val) {
        this.val = val;
    }
    int val;
}
