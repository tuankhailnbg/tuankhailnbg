//package nro.models.SieuHang;
//
//import java.sql.Connection;
//import java.sql.SQLException;
//import lombok.Getter;
//import lombok.Setter;
//import nro.consts.ConstPlayer;
//import nro.jdbc.DBService;
//import nro.jdbc.daos.GodGK;
//import nro.jdbc.daos.PlayerDAO;
//import nro.models.boss.Boss;
//import nro.models.boss.BossStatus;
//import nro.models.boss.dhvt.BossDHVT;
//import nro.models.map.DaiHoiVoThuat.DHVT23Service;
//import nro.models.player.Player;
//import nro.server.Client;
//import nro.services.EffectSkillService;
//import nro.services.ItemTimeService;
//import nro.services.PlayerService;
//import nro.services.Service;
//import nro.services.func.ChangeMapService;
//import nro.utils.Util;
//
///**
// *
// * @author louis
// */
//public class SieuHang_BackUp {
//
//    @Setter
//    @Getter
//    private Player player;
//    @Setter
//    private Boss boss;
//    @Setter
//    private int time;
//    @Setter
//    private int timeWait;
//
//    public void update() {
//        if (time > 0) {
//            time--;
//            if (player.isDie()) {
//                die();
//                return;
//            }
//            if (player.location != null && !player.isDie() && player != null && player.zone != null) {
//                if (boss.isDie()) {
//                    endChallenge();
//                    if (player.rankSieuHang == boss.rankSieuHang) {
//                        player.rankSieuHang--;
//                    } else if (player.rankSieuHang > boss.rankSieuHang) {
//                        long temp = player.rankSieuHang;
//                        player.rankSieuHang = boss.rankSieuHang;
//                        BossDHVT bossDHVT = (BossDHVT) boss;
//                        Player player1 = Client.gI().getPlayer(bossDHVT.idPlayer);
//                        if (player1 == null) {
//                            player1 = GodGK.loadByID(bossDHVT.idPlayer);
//                        }
//                        player1.rankSieuHang = (int) temp;
////                        Connection conn = DBService.gI().getConnectionForAutoSave();
////                        PlayerDAO.updatePlayer(player1, conn);
//                    }
//                    Service.gI().chat(player, "Haha thắng cuộc rồi! Đã thăng lên hạng " + player.rankSieuHang);
////                    Connection conn2 = DBService.gI().getConnectionForAutoSave();
////                    PlayerDAO.updatePlayer(player, conn2);
//                    boss.leaveMap();
//                }
//                if (player.location.y > 264) {
//                    leave();
//                }
//            } else {
//                if (boss != null) {
//                    boss.leaveMap();
//                }
//                SieuHangManager.gI().remove(this);
//            }
//        } else {
//            timeOut();
//        }
//        
//        if (timeWait > 0) {
//            switch (timeWait) {
//                case 5:
//                    ready();
//                    Service.getInstance().chat(boss, "Sẵn sàng chưa");
//                    break;
//                case 1:
//                    Service.getInstance().chat(player, "Ok");
//                    break;
//            }
//            timeWait--;
//        }
//        
//    }
//
//    public void ready() {
//        
//        EffectSkillService.gI().startStun(player, System.currentTimeMillis(), 10000);
//         
//        EffectSkillService.gI().startStun(boss, System.currentTimeMillis(), 10000);
//       
//        ItemTimeService.gI().sendItemTime(player, 3779, 10000 / 1000);
//        
//        Util.setTimeout(() -> {
//            SieuHangService.gI().sendTypePK(player, boss);
//            PlayerService.gI().changeAndSendTypePK(this.player, ConstPlayer.PK_PVP);
//            boss.setStatus((byte) 3);
//        }, 5000);
//    }
//
//    public void toTheNextRound(Boss bss) {
//        try {
//            PlayerService.gI().setPos(player, 335, 264, 0);
//            setTimeWait(5);
//            setBoss(boss);
//            System.out.println("OK");
//            setTime(185);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public Player getPlayer() {
//        return player;
//    }
//
//    public void setPlayer(Player player) {
//        this.player = player;
//    }
//
//    public Boss getBoss() {
//        return boss;
//    }
//
//    public void setBoss(Boss boss) {
//        this.boss = boss;
//    }
//
//    public int getTime() {
//        return time;
//    }
//
//    public void setTime(int time) {
//        this.time = time;
//    }
//
//    private void die() {
//        Service.getInstance().sendThongBao(player, "Thất bại rồi nhục nhã quá");
//        if (player.zone != null) {
//            endChallenge();
//        }
//    }
//
//    private void timeOut() {
//        Service.getInstance().sendThongBao(player, "Bạn bị xử thua vì hết thời gian");
//        endChallenge();
//    }
//
//    public void leave() {
//        setTime(0);
//        EffectSkillService.gI().removeStun(player);
//        Service.getInstance().sendThongBao(player, "Bạn bị xử thua vì rời khỏi võ đài");
//        endChallenge();
//    }
//
//    public void endChallenge() {
//        if (player.zone != null) {
//            PlayerService.gI().hoiSinh(player);
//        }
//        PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
//        if (player != null && player.zone != null && player.zone.map.mapId == 113) {
//            Util.setTimeout(() -> {
//                ChangeMapService.gI().changeMapNonSpaceship(player, 113, player.location.x, 360);
//            }, 500);
//        }
//        if (boss != null) {
//            boss.leaveMap();
//        }
//        SieuHangManager.gI().remove(this);
//    }
//}
