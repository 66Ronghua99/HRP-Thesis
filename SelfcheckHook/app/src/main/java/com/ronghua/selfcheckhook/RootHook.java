package com.ronghua.selfcheckhook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class RootHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.ronghua.selfcheck")) {
            XposedHelpers.findAndHookMethod("com.ronghua.selfcheck.RootDetection",
                    lpparam.classLoader, "isSuExists", new MethodHandler());
            XposedHelpers.findAndHookMethod("com.ronghua.selfcheck.RootDetection",
                    lpparam.classLoader, "suFileDetection", new MethodHandler());
            XposedHelpers.findAndHookMethod("com.ronghua.selfcheck.RootDetection",
                    lpparam.classLoader, "arePkgsInstalled", String[].class, new MethodHandler());
            XposedHelpers.findAndHookMethod("com.ronghua.selfcheck.RootDetection",
                    lpparam.classLoader, "mountPathsDetection", new MethodHandler());
        }
    }
}
