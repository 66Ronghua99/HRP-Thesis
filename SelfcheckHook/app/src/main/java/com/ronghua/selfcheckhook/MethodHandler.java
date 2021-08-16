package com.ronghua.selfcheckhook;

import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class MethodHandler extends XC_MethodHook {
    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        if(param.args.length>0 && param.args[0] instanceof String[]){
            param.args[0] = new String[]{};
        }
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        param.setResult(false);
        if(param.method.getName().equals("arePkgsInstalled"))
            return;
        ArrayList<String> list = (ArrayList<String>) XposedHelpers.getObjectField(param.thisObject, "rootTraitsList");
        list.remove(list.size()-1);
    }
}
