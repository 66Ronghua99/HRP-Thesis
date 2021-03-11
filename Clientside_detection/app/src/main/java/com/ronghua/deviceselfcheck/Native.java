package com.ronghua.deviceselfcheck;

public class Native {
    static {
        System.loadLibrary("native-lib");
    }

    public static native boolean detectMagiskNative();
}
