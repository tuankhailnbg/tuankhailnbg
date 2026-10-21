package nro.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class FileUtils {

    public static void writeFile(String fileName, String text) {
        try {
            File folder = new File("log");
            if (!folder.exists()) {
                folder.mkdir();
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter("log/" + fileName + ".txt"));
            bw.write(text);
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static long getFolderSize(File folder) {
        long length = 0;
        File[] files = folder.listFiles();
        int count = files.length;
        for (int i = 0; i < count; i++) {
            if (files[i].isFile()) {
                length += files[i].length();
            } else {
                length += getFolderSize(files[i]);
            }
        }
        return length;
    }

    public static String cutPng(String str) {
        String result = str;
        if (str.contains(".png")) {
            result = str.replace(".png", "");
        }
        return result;
    }

    public static void addPath(ArrayList<File> list, File file) {
        if (file.isFile()) {
            list.add(file);
        } else {
            for (File f : file.listFiles()) {
                addPath(list, f);
            }
        }
    }

}
