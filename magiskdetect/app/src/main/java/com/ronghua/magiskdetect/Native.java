package com.ronghua.magiskdetect;

public class Native {

    static {
        System.loadLibrary("native-lib");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public static native boolean DetectMagisk();
}
