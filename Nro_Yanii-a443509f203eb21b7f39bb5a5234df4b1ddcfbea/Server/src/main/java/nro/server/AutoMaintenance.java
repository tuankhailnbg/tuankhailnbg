/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nro.server;

import nro.models.player.Player;
import nro.services.Service;
import java.io.IOException;
import java.time.LocalTime;

/**
 *
 * @author Administrator
 */
public class AutoMaintenance extends Thread {
    public static boolean AutoMaintenance = true;// Bật, Tắt Bảo trì tự động
    public static final int hours = 06;// giờ bảo trì
    public static final int hours_1 = 19;// giờ bảo trì
    public static final int mins = 00;// phút bảo trì
    private static AutoMaintenance instance;
    public static boolean isRunning;

    public static AutoMaintenance gI() {
        if (instance == null) {
            instance = new AutoMaintenance();
        }
        return instance;
    }

    @Override
    public void run() {
        while (!Maintenance.isRuning && !isRunning) {
            Player player = null;
            try {
                if (AutoMaintenance) {
                    LocalTime currentTime = LocalTime.now();
                    if ((currentTime.getHour() == hours || currentTime.getHour() == hours_1)
                            && currentTime.getMinute() == mins) {
                        Maintenance.gI().start(60);/// SỐ GIÂY BẢO TRÌ KHÔNG DƯỚI 60S
                        Service.getInstance().sendThongBaoAllPlayer(
                                "Hệ thống bảo trì định kỳ, vui lòng thoát game để tránh mất vật phẩm");

                        isRunning = true;
                        AutoMaintenance = false;
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    public static void runBatchFile(String batchFilePath) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "start", batchFilePath);
        Process process = processBuilder.start();
        try {
            process.waitFor();
        } catch (Exception e) {

        }
    }
}
