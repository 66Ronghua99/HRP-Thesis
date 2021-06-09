package com.ronghua.bledetect.bleadv;


import com.ronghua.bledetect.authentication.Authentication;

public class Listener {
    private static Listener instance;
    private Authentication auth;
    private BlueToothUtils bleUtils;


    public static Listener getInstance() {
        if (instance ==  null)
            instance = new Listener();
        return instance;
    }
}
