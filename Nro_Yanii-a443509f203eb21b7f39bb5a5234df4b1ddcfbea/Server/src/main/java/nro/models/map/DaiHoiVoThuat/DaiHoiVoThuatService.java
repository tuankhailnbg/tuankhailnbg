package nro.models.map.DaiHoiVoThuat;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import nro.models.map.Map;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.server.Client;
import nro.server.ServerNotify;
import nro.server.io.Message;
import nro.services.MapService;
import nro.services.Service;
import nro.services.func.ChangeMapService;
import nro.utils.Util;

/**
 *
 * @author louis
 */
public class DaiHoiVoThuatService {

    private static DaiHoiVoThuatService instance;

    public static DaiHoiVoThuatService gI() {
        if (instance == null) {
            instance = new DaiHoiVoThuatService();
        }
        return instance;
    }

    public void initDaiHoiVoThuat() {
        Timer timerDHVT = new Timer();
        TimerTask DHVT = new TimerTask() {
            public void run() {
                DaiHoiVoThuatManager.gI().lstIDPlayers.clear();
                DaiHoiVoThuatManager.gI().lstIDPlayers2.clear();
                DaiHoiVoThuatManager.gI().openDHVT = false;
                DaiHoiVoThuatManager.gI().roundNow = (byte) 0;
                int crrHOUR = DaiHoiVoThuatManager.gI().hourDHVT;
                DaiHoiVoThuatManager.gI().typeDHVT = hourToTypeDHVT(crrHOUR);
                Timer timerReset = new Timer();
                TimerTask OpenReset = new TimerTask() {
                    public void run() {
                        DaiHoiVoThuatManager.gI().hourDHVT = (DaiHoiVoThuatManager.gI().hourDHVT + 1) >= 24 ? 0 : (DaiHoiVoThuatManager.gI().hourDHVT + 1);
                        timerReset.cancel();
                    }
                ;
                };
                timerReset.schedule(OpenReset, 5000);

                if (DaiHoiVoThuatManager.gI().typeDHVT > (byte) 0) {
                    //SET LAI TIME
                    Calendar calendar = Calendar.getInstance();
                    int minuteStart = calendar.get(Calendar.MINUTE);
                    DaiHoiVoThuatManager.gI().openDHVT = true;
                    DaiHoiVoThuatManager.gI().tOpenDHVT = System.currentTimeMillis() + 1800000 - (minuteStart * 60000);
                    //TASK THONG BAO DEN GIO THI DAU
                    Timer timerOpenDHVT = new Timer();
                    TimerTask OpenDHVT = new TimerTask() {
                        public void run() {
                            if ((System.currentTimeMillis() - DaiHoiVoThuatManager.gI().tOpenDHVT) >= 0) {
                                //CALL TASK CHIA TRAN DAU
                                matchDHVT();
                                timerOpenDHVT.cancel();
                            }
                            DaiHoiVoThuatManager.gI().openDHVT = true;
                            Player _p = null;
                            for (int i = 0; i < DaiHoiVoThuatManager.gI().lstIDPlayers.size(); i++) {
                                _p = Client.gI().getPlayer(DaiHoiVoThuatManager.gI().lstIDPlayers.get(i));
                                if (_p != null && _p.getSession() != null && _p.zone.map.mapId == 52) {
                                    Service.getInstance().sendThongBao(_p, "Trận đấu của bạn sẽ diễn ra trong vòng " + (int) ((DaiHoiVoThuatManager.gI().tOpenDHVT - System.currentTimeMillis()) / 60000) + " phút nữa");
                                }
                            }
                        }
                    ;
                    };
                    timerOpenDHVT.schedule(OpenDHVT, 0, 30000);
                }
            }
        ;
        };

        Calendar calendar = Calendar.getInstance();
        calendar.get(Calendar.HOUR_OF_DAY);
        int crrHOUR = calendar.get(Calendar.HOUR_OF_DAY);
        int crrMINUTE = calendar.get(Calendar.MINUTE);
        Date dateSchedule = calendar.getTime();
        if (crrMINUTE < 30) {
            calendar.set(Calendar.HOUR_OF_DAY, crrHOUR);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            dateSchedule = calendar.getTime();
            DaiHoiVoThuatManager.gI().typeDHVT = hourToTypeDHVT(crrHOUR);
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, (crrHOUR + 1));
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            dateSchedule = calendar.getTime();
            DaiHoiVoThuatManager.gI().typeDHVT = hourToTypeDHVT(crrHOUR + 12);
        }
        DaiHoiVoThuatManager.gI().hourDHVT = calendar.get(Calendar.HOUR_OF_DAY);
        long period = 60 * 60 * 1000;
        timerDHVT.schedule(DHVT, dateSchedule, period);
    }

    public byte hourToTypeDHVT(int hour) {
        // Đảm bảo giờ luôn nằm trong khoảng từ 0 đến 23
        hour = (hour >= 24) ? hour - 24 : hour;

//        System.out.println("Đại hội võ thuật hour: " + hour);
        if (hour == 8 || hour == 13 || hour == 18) {
            return 1;
        } else if (hour == 9 || hour == 14 || hour == 19) {
            return 2;
        } else if (hour == 10 || hour == 15 || hour == 20) {
            return 3;
        } else if (hour == 11 || hour == 16 || hour == 21) {
            return 4;
        } else if (hour == 12 || hour == 17 || hour == 22 || hour == 23 || hour == 0) {
            return 5;
        } else {
            return 0;
        }
    }

    public boolean canRegisDHVT(long sucManh) {
        if (DaiHoiVoThuatManager.gI().typeDHVT == (byte) 1 && sucManh < 1500000L) {
            return true;
        } else if (DaiHoiVoThuatManager.gI().typeDHVT == (byte) 2 && sucManh >= 1500000L && sucManh < 15000000L) {
            return true;
        } else if (DaiHoiVoThuatManager.gI().typeDHVT == (byte) 3 && sucManh >= 15000000L && sucManh < 150000000L) {
            return true;
        } else if (DaiHoiVoThuatManager.gI().typeDHVT == (byte) 4 && sucManh >= 150000000L && sucManh < 1500000000L) {
            return true;
        } else if (DaiHoiVoThuatManager.gI().typeDHVT == (byte) 5) {
            return true;
        }
        return false;
    }

    public String textDaiHoi(long sucManh) {
        if (sucManh < 1500000L) {
            return "|7|[Bạn chỉ có thể tham gia giải Ngoại hạng và Nhi đồng]";
        } else if (sucManh >= 1500000L && sucManh < 15000000L) {
            return "|7|[Bạn chỉ có thể tham gia giải Ngoại hạng và Siêu cấp 1]";
        } else if (sucManh >= 15000000L && sucManh < 150000000L) {
            return "|7|[Bạn chỉ có thể tham gia giải Ngoại hạng và Siêu cấp 2]";
        } else if (sucManh >= 150000000L && sucManh < 1500000000L) {
            return "|7|[Bạn chỉ có thể tham gia giải Ngoại hạng và Siêu cấp 3]";
        } else if (sucManh > 1500000000L) {
            return "|7|[Bạn chỉ có thể tham gia giải Ngoại hạng]";
        } else {
            return null;
        }
    }

    public String textDaiHoi2(long sucManh) {
        if (sucManh < 1500000L) {
            return "Bạn chỉ có thể tham gia giải Ngoại hạng và Nhi đồng";
        } else if (sucManh >= 1500000L && sucManh < 15000000L) {
            return "Bạn chỉ có thể tham gia giải Ngoại hạng và Siêu cấp 1";
        } else if (sucManh >= 15000000L && sucManh < 150000000L) {
            return "Bạn chỉ có thể tham gia giải Ngoại hạng và Siêu cấp 2";
        } else if (sucManh >= 150000000L && sucManh < 1500000000L) {
            return "Bạn chỉ có thể tham gia giải Ngoại hạng và Siêu cấp 3";
        } else if (sucManh > 1500000000L) {
            return "Bạn chỉ có thể tham gia giải Ngoại hạng";
        } else {
            return null;
        }
    }

    public void startVSDHVT(Player p, Player pVS, byte type) {
        Message m = null;
        try {
            m = new Message(-30);
            m.writer().writeByte((byte) 35);
            m.writer().writeInt((int) p.id); //ID PLAYER
            m.writer().writeByte(type); //TYPE PK
            m.writer().flush();
            p.getSession().sendMessage(m);
            pVS.getSession().sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (m != null) {
                m.cleanup();
            }
        }
    }

    public void updateTypePK(Player p, byte type) {
        Message m = null;
        try {
            m = new Message(-30);
            m.writer().writeByte((byte) 35);
            m.writer().writeInt((int) p.id); //ID PLAYER
            m.writer().writeByte(type); //TYPE PK
            m.writer().flush();
            p.getSession().sendMessage(m);
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (m != null) {
                m.cleanup();
            }
        }
    }

    public void matchDHVT() {
        int countPlayer = DaiHoiVoThuatManager.gI().lstIDPlayers.size();
        DaiHoiVoThuatManager.gI().roundNow += (byte) 1;
        if (countPlayer > 1) {
            int countMatch = (int) (countPlayer / 2);
            DaiHoiVoThuatManager.gI().tNextRound = System.currentTimeMillis() + 130000;
            //INIT DHVT
            Map dhvt = MapService.gI().getMapById(51);
            DaiHoiVoThuatManager.gI().mapDhvt = dhvt;
            for (int i = 0; i < countMatch; i++) {
                //GET PLAYER 1
                int index1 = Util.nextIntDhvt(0, DaiHoiVoThuatManager.gI().lstIDPlayers.size());
                long id1 = DaiHoiVoThuatManager.gI().lstIDPlayers.get(index1);
                Player p1 = Client.gI().getPlayer(id1);
                if (p1 != null && p1.getSession() != null) {
                    if (p1.zone.map.mapId == 52) {
                        if (p1.pet != null) {
                            p1.pet.changeStatus(Pet.GOHOME);
                        }
                        ChangeMapService.gI().changeMap(p1, 51, i, 300, 312);
                        DaiHoiVoThuatManager.gI().lstIDPlayers2.add(id1);
                        DaiHoiVoThuatManager.gI().lstIDPlayers.remove(index1);
                    } else {
                        DaiHoiVoThuatManager.gI().lstIDPlayers.remove(index1);
                    }
                }
                //GET PLAYER 2
                index1 = Util.nextIntDhvt(0, DaiHoiVoThuatManager.gI().lstIDPlayers.size());
                long id2 = DaiHoiVoThuatManager.gI().lstIDPlayers.get(index1);
                Player p2 = Client.gI().getPlayer(id2);
                if (p2 != null && p2.getSession() != null) {
                    if (p2.zone.map.mapId == 52) {
                        if (p2.pet != null) {
                            p2.pet.changeStatus(Pet.GOHOME);
                        }
                        ChangeMapService.gI().changeMap(p2, 51, i, 468, 312);
                        DaiHoiVoThuatManager.gI().lstIDPlayers2.add(id2);
                        DaiHoiVoThuatManager.gI().lstIDPlayers.remove(index1);
                    } else {
                        DaiHoiVoThuatManager.gI().lstIDPlayers.remove(index1);
                    }
                }
                Timer timerVS = new Timer();
                TimerTask vsDHVT = new TimerTask() {
                    public void run() {
                        Player p11 = Client.gI().getPlayer(id1);
                        Player p22 = Client.gI().getPlayer(id2);
                        if ((p11 == null || p11.getSession() == null) && p22 != null && p22.getSession() != null) {
                            winRoundDHVT(p22, p11);
                        } else if ((p22 == null || p22.getSession() == null) && p11 != null && p11.getSession() != null) {
                            winRoundDHVT(p11, p22);
                        } else if (p22 != null && p22.getSession() != null && p11 != null && p11.getSession() != null) {
                            if (p11.zone.map.mapId != 51) {
                                winRoundDHVT(p22, p11);
                            } else if (p22.zone.map.mapId != 51) {
                                winRoundDHVT(p11, p22);
                            } else {
//                                try {
//                                    Thread.sleep(60000);
                                p11.typePk = (byte) 3;
                                p22.typePk = (byte) 3;
//                                    remove_player(p11.zone, -251003);
//                                    Message msg;
//                                    try {
//                                        msg = new Message(-6);
//                                        msg.writer().writeInt(-251003);
//                                        Service.getInstance().sendMessAllPlayerInMap(p11.zone, msg);
//                                        Service.getInstance().sendMessAllPlayerInMap(p22.zone, msg);
//                                        msg.cleanup();
//                                    } catch (Exception ex) {
//                                    }
                                p11.lockPK = true;
                                p22.lockPK = true;
                                startVSDHVT(p11, p22, (byte) 3);
                                startVSDHVT(p22, p11, (byte) 3);
//                                } catch (InterruptedException ex) {
//                                    Logger.getLogger(DaiHoiVoThuatService.class.getName()).log(Level.SEVERE, null, ex);
//                                }
                            }
                        }
                        timerVS.cancel();
                    }
                ;
                };
                timerVS.schedule(vsDHVT, 5000);
                //TASK QUYET DINH NGUOI CHIEN THANG
                Timer timerWIN = new Timer();
                TimerTask winDHVT = new TimerTask() {
                    public void run() {
                        Player p11 = Client.gI().getPlayer(id1);
                        Player p22 = Client.gI().getPlayer(id2);
                        if ((p11 == null || p11.getSession() == null) && p22 != null && p22.getSession() != null) {
                            winRoundDHVT(p22, p11);
                        } else if ((p22 == null || p22.getSession() == null) && p11 != null && p11.getSession() != null) {
                            winRoundDHVT(p11, p22);
                        } else if (p11 != null && p11.getSession() != null && p22 != null && p22.getSession() != null) {
                            if (p11.zone.map.mapId != 51) {
                                winRoundDHVT(p22, p11);
                            } else if (p22.zone.map.mapId != 51) {
                                winRoundDHVT(p11, p22);
                            } else {
                                if (!p11.isDie() && !p22.isDie()) {
                                    p11.typePk = (byte) 0;
                                    p22.typePk = (byte) 0;
                                    p11.lockPK = false;
                                    p22.lockPK = false;
                                    if (p11.nPoint.hp >= p22.nPoint.hp) {
                                        DaiHoiVoThuatManager.gI().lstIDPlayers.add(p11.id);
                                        Service.getInstance().sendThongBao(p11, "Bạn đã chiến thắng, bạn nhận được " + DaiHoiVoThuatManager.gI().costRoundDHVT());
                                        //CHECK NHIEM VU VONG 2 DHVT
                                        startVSDHVT(p11, p22, (byte) 0);
                                        ChangeMapService.gI().changeMapInYard(p11, 52, -1, -1);
                                        Service.getInstance().hsChar(p22, 1, 1);
                                        ChangeMapService.gI().changeMapInYard(p22, 52, -1, -1);
                                        Service.getInstance().sendThongBao(p22, "Bạn đã thua, hẹn gặp lại ở giải sau");
                                    } else {
                                        DaiHoiVoThuatManager.gI().lstIDPlayers.add(p22.id);
                                        Service.getInstance().sendThongBao(p22, "Bạn đã chiến thắng, bạn nhận được " + DaiHoiVoThuatManager.gI().costRoundDHVT());
                                        //CHECK NHIEM VU VONG 2 DHVT
                                        startVSDHVT(p22, p11, (byte) 0);
                                        ChangeMapService.gI().changeMapInYard(p22, 52, -1, -1);
                                        Service.getInstance().hsChar(p11, 1, 1);
                                        ChangeMapService.gI().changeMapInYard(p11, 52, -1, -1);
                                        Service.getInstance().sendThongBao(p11, "Bạn đã thua, hẹn gặp lại ở giải sau");
                                    }
                                    timerVS.cancel();
                                } else if (!p11.isDie() && p22.isDie()) {
                                    DaiHoiVoThuatManager.gI().lstIDPlayers.add(p11.id);
                                    Service.getInstance().sendThongBao(p11, "Bạn đã chiến thắng, bạn nhận được " + DaiHoiVoThuatManager.gI().costRoundDHVT());
                                    //CHECK NHIEM VU VONG 2 DHVT
                                    startVSDHVT(p11, p22, (byte) 0);
                                    ChangeMapService.gI().changeMapInYard(p11, 52, -1, -1);
                                    Service.getInstance().hsChar(p22, 1, 1);
                                    ChangeMapService.gI().changeMapInYard(p22, 52, -1, -1);
                                    Service.getInstance().sendThongBao(p22, "Bạn đã thua, hẹn gặp lại ở giải sau");
                                } else if (p11.isDie() && !p22.isDie()) {
                                    DaiHoiVoThuatManager.gI().lstIDPlayers.add(p22.id);
                                    Service.getInstance().sendThongBao(p22, "Bạn đã chiến thắng, bạn nhận được " + DaiHoiVoThuatManager.gI().costRoundDHVT());
                                    //CHECK NHIEM VU VONG 2 DHVT
                                    startVSDHVT(p22, p11, (byte) 0);
                                    ChangeMapService.gI().changeMapInYard(p22, 52, -1, -1);
                                    Service.getInstance().hsChar(p11, 1, 1);
                                    ChangeMapService.gI().changeMapInYard(p11, 52, -1, -1);
                                    Service.getInstance().sendThongBao(p11, "Bạn đã thua, hẹn gặp lại ở giải sau");
                                } else {
                                    timerVS.cancel();
                                }
                            }
                        } else {
                            timerVS.cancel();
                        }
                    }
                ;
                };
                timerWIN.schedule(winDHVT, 65000);
                p1.timerDHVT = timerWIN;
                p1._friendGiaoDich = p2;
                p2.timerDHVT = timerWIN;
                p2._friendGiaoDich = p1;
            }
            if (DaiHoiVoThuatManager.gI().lstPlayers.size() > 0) {
                Service.getInstance().sendThongBao(Client.gI().getPlayer(DaiHoiVoThuatManager.gI().lstIDPlayers.get(0)), "Bạn được vào vòng tiếp theo");
                //CHECK NHIEM VU VONG 2 DHVT
                Service.getInstance().sendThongBaoOK(DaiHoiVoThuatManager.gI().lstPlayers.get(0), "Bạn được vào vòng tiếp theo");
            }
            Timer timerRestart = new Timer();
            TimerTask restartDHVT = new TimerTask() {
                public void run() {
                    if ((System.currentTimeMillis() - DaiHoiVoThuatManager.gI().tNextRound) >= 0) {
                        //RESTART VONG TIEP THEO
                        DaiHoiVoThuatManager.gI().lstIDPlayers2.clear();
                        matchDHVT();
                        timerRestart.cancel();
                    } else {
                        if (DaiHoiVoThuatManager.gI().lstIDPlayers.size() > 1) {
                            Player _p = null;
                            for (int i = 0; i < DaiHoiVoThuatManager.gI().lstIDPlayers.size(); i++) {
                                _p = Client.gI().getPlayer(DaiHoiVoThuatManager.gI().lstIDPlayers.get(i));
                                if (_p != null && _p.getSession() != null) {
                                    Service.getInstance().sendThongBao(_p, "Trận đấu của bạn sẽ diễn ra trong vòng " + (int) ((DaiHoiVoThuatManager.gI().tNextRound - System.currentTimeMillis()) / 1000) + " giây nữa");
                                }
                            }
                        } else {
                            //THONG BAO VO DICH VA END TASK
                            if (DaiHoiVoThuatManager.gI().lstIDPlayers.size() == 1) {
                                ServerNotify.gI().notify(Client.gI().getPlayer(DaiHoiVoThuatManager.gI().lstIDPlayers.get(0)).name + " đã vô địch giải " + DaiHoiVoThuatManager.gI().nameRoundDHVT() + " mọi người đều thán phục!");
                                Service.getInstance().sendThongBao(Client.gI().getPlayer(DaiHoiVoThuatManager.gI().lstIDPlayers.get(0)), "Bạn đã vô địch giải đấu, xin chúc mừng bạn, bạn được thưởng 5 viên đá nâng cấp");
                            }
                            DaiHoiVoThuatManager.gI().lstIDPlayers.clear();
                            DaiHoiVoThuatManager.gI().lstIDPlayers2.clear();
                            DaiHoiVoThuatManager.gI().openDHVT = false;
                            DaiHoiVoThuatManager.gI().roundNow = (byte) 0;
                            timerRestart.cancel();
                        }
                    }
                }
            ;
            };
            timerRestart.schedule(restartDHVT, 70000, 10000);
        } else {
            //THONG BAO VO DICH
            if (countPlayer == 1) {
                Service.getInstance().sendThongBao(Client.gI().getPlayer(DaiHoiVoThuatManager.gI().lstIDPlayers.get(0)), "Bạn đã vô địch giải đấu, xin chúc mừng bạn, bạn được thưởng 100 ngọc");
            }
            DaiHoiVoThuatManager.gI().lstIDPlayers.clear();
            DaiHoiVoThuatManager.gI().lstIDPlayers2.clear();
            DaiHoiVoThuatManager.gI().openDHVT = false;
            DaiHoiVoThuatManager.gI().roundNow = (byte) 0;
        }
    }

    public void winRoundDHVT(Player pW, Player pL) {
        if (pW != null && pW.getSession() != null) {
            Service.getInstance().sendThongBao(pW, "Đối thủ đã kiệt sức hoặc rơi đài, bạn đã thắng");
            Service.getInstance().sendThongBao(pW, "Bạn vừa nhận thưởng " + DaiHoiVoThuatManager.gI().costRoundDHVT());
            //CHECK NHIEM VU VONG 2 DHVT
        }
        Timer timerWIN = new Timer();
        TimerTask winDHVT = new TimerTask() {
            public void run() {
                if (pW != null && pW.getSession() != null) {
                    DaiHoiVoThuatManager.gI().lstIDPlayers.add(pW.id);
                    pW.typePk = (byte) 0;
                    pW.lockPK = false;
                    updateTypePK(pW, (byte) 0);
                    pW._friendGiaoDich = null;
                    if (pW.timerDHVT != null) {
                        pW.timerDHVT.cancel();
                        pW.timerDHVT = null;
                    }
                    ChangeMapService.gI().changeMapInYard(pW, 52, -1, -1);
                }
                if (pL != null && pL.getSession() != null) {
                    pL.typePk = (byte) 0;
                    pL.lockPK = false;
                    updateTypePK(pL, (byte) 0);
                    pL._friendGiaoDich = null;
                    if (pL.timerDHVT != null) {
                        pL.timerDHVT.cancel();
                        pL.timerDHVT = null;
                    }
                    Service.getInstance().hsChar(pL, 1, 1);
                    ChangeMapService.gI().changeMapInYard(pL, 52, -1, -1);
                    Service.getInstance().sendThongBao(pL, "Bạn đã thua, hẹn gặp lại ở giải sau");
                }
            }
        ;
        };
        timerWIN.schedule(winDHVT, 5000);
    }
}
