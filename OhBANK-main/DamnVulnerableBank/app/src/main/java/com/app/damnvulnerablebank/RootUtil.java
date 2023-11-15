package com.app.damnvulnerablebank;

import android.app.ActivityManager;
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
        return isAppInstalled() || isBinaryExists() || isCommandExists() || buildPropCheck() || isSetUIDFileExists() || readDefaultProp() || rootingFileCheck();
                //isAppInstalled() || isBinaryExists() || isCommandExists() || buildPropCheck() || isSetUIDFileExists() || readDefaultProp() || rootingFileCheck();
    }

    // 루팅 관련 앱(패키지)을 탐지
    public static boolean isAppInstalled() {
        String[] rootedApps = {
                "com.noshufou.android.su",
                "com.thirdparty.superuser",
                "eu.chainfire.supersu",
                "com.koushikdutta.superuser",
        };

        try {
            for (String packageName : rootedApps) {
                Process process = Runtime.getRuntime().exec("pm list packages " + packageName);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(packageName)) {
                        return true;
                    }
                }
                process.waitFor(); // 프로세스가 완료될 때까지 대기
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 바이너리(실행 파일)가 시스템에 존재하는지 확인
    private static boolean isBinaryExists() {
        Process process = null;
        String[] rootedBinaries = {
                "su",
                "busybox"
        };
        try {
            for (String binary : rootedBinaries) {
                process = Runtime.getRuntime().exec(new String[] { "which", binary });
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                if (in.readLine() != null) return true;
            }
            return false;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

    // 명령어가 시스템에서 실행 가능한지 여부를 확인
    public static boolean isCommandExists() {
        String[] rootedCommands = {
                "su",
                "busybox"
        };

        try {
            for (String command : rootedCommands) {
                Process process = Runtime.getRuntime().exec(new String[] { "/system/bin/sh", "-c", "which " + command });
                int exitValue = process.waitFor();
                if (exitValue == 0) return true; // exitValue가 0이면 명령어가 존재함
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    // build.prop (build tags 값 확인)
    private static boolean buildPropCheck() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
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

    // 루팅 관련 파일 확인
    private static boolean rootingFileCheck() {
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

//    private static boolean usbdebugcheck(){
//        boolean isDebuggable =  ( 0 != ( getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE ));
//
//        if() {
//            // debugging enabled
//        } else {
//            //;debugging does not enabled
//        }
//    }
}