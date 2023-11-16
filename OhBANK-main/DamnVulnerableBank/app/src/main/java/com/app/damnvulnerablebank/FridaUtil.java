package com.app.damnvulnerablebank;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FridaUtil {
    // 다양한 포트에서 연결 시도하여 프리다와의 연결 여부를 확인
    static volatile boolean fridaDetected = false; // 프리다 탐지 여부를 저장하는 변수

    public static void detectFrida(int startPort, int endPort) {
        int numThreads = Runtime.getRuntime().availableProcessors(); // 사용 가능한 프로세서 수
        int portsPerThread = (endPort - startPort + 1) / numThreads;

        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            int fromPort = startPort + (i * portsPerThread);
            int toPort = i == numThreads - 1 ? endPort : fromPort + portsPerThread - 1;
            threads[i] = new FridaDetectionThread(fromPort, toPort);
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class FridaDetectionThread extends Thread {
        private final int startPort;
        private final int endPort;

        public FridaDetectionThread(int startPort, int endPort) {
            this.startPort = startPort;
            this.endPort = endPort;
        }

        @Override
        public void run() {
            for (int port = startPort; port <= endPort; port++) {
                if (fridaDetected) { // 다른 스레드에서 프리다를 이미 감지한 경우
                    return; // 탐지 로직 멈추고 종료
                }
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress("127.0.0.1", port), 1000); // 1초 내에 연결 시도
                    System.out.println("Frida detected on port: " + port);
                    fridaDetected = true; // 프리다를 발견했으므로 변수를 true로 설정
                    socket.close();
                    return; // 프리다를 감지했으므로 탐지 로직을 멈추고 종료
                } catch (Exception e) {
                    // 연결 실패 시 다음 포트로 진행
                }
            }
        }
    }

    // 시스템 리소스 및 파일 확인하여 프리다의 존재 여부를 검사
    public static boolean checkFridaFiles() {
        String[] fridaFiles = {
                "/data/local/tmp/frida-server",
                "/data/local/frida-server",
                "/system/bin/frida-server",
                "/sbin/frida-server"
        };

        for (String filePath : fridaFiles) {
            File file = new File(filePath);
            if (file.exists()) {
                return true; // 파일이 존재하면 프리다로 간주
            }
        }
        return false; // 파일이 존재하지 않으면 프리다가 아닌 것으로 간주
    }

    // 프로세스 목록 확인하여 프리다 서버의 프로세스를 감지
    static boolean checkFridaProcesses() {
        try {
            Process process = Runtime.getRuntime().exec("ps");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("frida-server")) {
                    return true; // 프로세스가 발견되면 프리다로 간주
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; // 프로세스가 발견되지 않으면 프리다가 아닌 것으로 간주
    }
}
