package com.ronghua.selfcheck;

public class Native {
    static {
        System.loadLibrary("native-lib");
    }

    public static native void isLibLoaded();

    public static native boolean detectMagiskNative();
}
