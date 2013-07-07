package org.rickosborne.tubetastic.android;

import java.util.Random;

public enum RandomService {
    INSTANCE;

    private static Random random = new Random();

    public static Random getRandom() { return random; }

}
