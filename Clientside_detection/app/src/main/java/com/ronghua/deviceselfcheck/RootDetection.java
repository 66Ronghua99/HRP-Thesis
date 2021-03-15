package com.ronghua.deviceselfcheck;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

public class RootDetection {
    private static String TAG = "RootDetection";
    private Context mContext;

    public RootDetection(Context context){
        mContext = context;
    }

    public boolean isRooted(){
        return suFileDetection();
//                buildTagDetection()||mountPathsDetection()||rootAppDetection()||dangerousAppDetection()
//                ||rootCloakingAppDetection();
    }

    private boolean rootAppDetection(){
        return arePkgsInstalled(Const.knownRootAppsPackages);
    }

    private boolean dangerousAppDetection(){
        return arePkgsInstalled(Const.knownDangerousAppsPackages);
    }

    private boolean rootCloakingAppDetection(){
        return arePkgsInstalled(Const.knownRootCloakingPackages);
    }

    private boolean buildTagDetection(){
        String buildTags = android.os.Build.TAGS;
        Log.i(TAG, "Build tags: "+ buildTags);
        return buildTags != null && buildTags.contains("test-keys");
    }

    //Haven't considered SDK version difference
    private boolean mountPathsDetection(){
        try {
            FileReader fr = new FileReader("/proc/self/mounts");
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while((line = br.readLine()) != null){
                String[] args = line.split(" ");
                for(String path:Const.pathsThatShouldNotBeWritable){
                    if(args[1].contains(path) && args[3].equals("rw")){
                        return true;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean arePkgsInstalled(String[] suspects){
        PackageManager pm = mContext.getPackageManager();
        for(String pkg: suspects){
            try {
                pm.getPackageInfo(pkg, 0);
                Log.i(TAG, "Root related app "+ pkg + " is detected!");
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                //Do nothing
            }
        }
        return false;
    }

    private boolean suFileDetection(){
        return filePathDetection("su")||filePathDetection("magisk")
                ||filePathDetection("busybox");
    }

    private boolean filePathDetection(String filename){
        boolean isSuDetected = false;
        String[] suPaths = Const.getPaths();
        for(String path: suPaths) {
            Log.i(TAG, "file: " + path + filename);
            File file = new File(path, filename);
            if(file.exists()){
                isSuDetected = true;
                break;
            }
        }
        return isSuDetected;
    }
}
