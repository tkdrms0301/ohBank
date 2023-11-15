package com.app.damnvulnerablebank;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class RootUtil {

    public static boolean isDeviceRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3() || isSetUIDFileExists() || readDefaultProp();
    }

    // build.prop (build tags)
    private static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    /*private static boolean usbdebugcheck(){
        boolean isDebuggable =  ( 0 != ( getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE ));

        if() {
            // debugging enabled
        } else {
            //;debugging does not enabled
        }
    }*/

    // 루팅 관련 파일 확인
    private static boolean checkRootMethod2() {
        // 루팅 관련 파일 경로
        String[] paths = {
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        };
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    // 명령어를 실행하여 su 바이너리 확인
    private static boolean checkRootMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[] { "/system/xbin/which", "su" });
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) return true;
            return false;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

    // 특정 디렉터리에 SetUID 가 존재하는 파일 확인
    public static boolean isSetUIDFileExists() {
        try {
            Process process = new ProcessBuilder("find", "/system", "-type", "f", "-perm", "-4000").start();
            return process.waitFor() == 0;
        } catch (InterruptedException | IOException e) {
            Log.e("RootUtil", "isSetUIDFileExists | IOException", e);
            return false;
        }
    }

    // default.prop 파일 읽기 (루팅된 기기에서만 동작함)
    public static boolean readDefaultProp() {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            os.writeBytes("cat /system/build.prop\n");
            os.writeBytes("exit\n");
            os.flush();

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return !output.toString().equals("");
    }
}