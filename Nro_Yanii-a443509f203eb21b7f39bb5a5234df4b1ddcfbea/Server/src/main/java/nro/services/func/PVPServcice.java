package nro.services.func;

import nro.consts.ConstNpc;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.models.pvp.ChallengePVP;
import nro.models.pvp.PVP;
import nro.models.pvp.RevengePVP;
import nro.server.Manager;
import nro.server.io.Message;
import nro.services.NpcService;
import nro.services.PlayerService;
import nro.services.Service;
import nro.utils.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class PVPServcice implements Runnable {

    private static final int[] GOLD_CHALLENGE = { 1000000, 10000000, 100000000 };
    private String[] optionsGoldChallenge;

    // cmd controller
    private static final byte OPEN_GOLD_SELECT = 0;
    private static final byte ACCEPT_PVP = 1;

    private static PVPServcice i;

    private static List<PVP> PVPS = new ArrayList<>();
    private static Map<Player, Player> PLAYER_VS_PLAYER = new HashMap<Player, Player>();
    private static Map<Player, PVP> PLAYER_PVP = new HashMap<Player, PVP>();
    private static Map<Player, Integer> PLAYER_GOLD = new HashMap<Player, Integer>();

    private PVPServcice() {
        this.optionsGoldChallenge = new String[GOLD_CHALLENGE.length];
        for (int i = 0; i < GOLD_CHALLENGE.length; i++) {
            this.optionsGoldChallenge[i] = Util.numberToMoney(GOLD_CHALLENGE[i]) + " vàng";
        }
    }

    public static PVPServcice gI() {
        if (i == null) {
            i = new PVPServcice();
            new Thread(i).start();
        }
        return i;
    }

    public void controller(Player player, Message message) {
        try {
            byte action = message.reader().readByte();
            byte type = message.reader().readByte();
            int playerId = message.reader().readInt();
            Player plMap = player.zone.getPlayerInMap(playerId);
            if (plMap != null) {
                PLAYER_VS_PLAYER.put(player, plMap);
                PLAYER_VS_PLAYER.put(plMap, player);
                switch (action) {
                    case OPEN_GOLD_SELECT:
                        openSelectGold(player, plMap);
                        break;
                    case ACCEPT_PVP:
                        acceptPVP(player);
                        break;
                }
            }
        } catch (IOException ex) {

        }
    }

    private void openSelectGold(Player pl, Player plMap) {
        PVP pvp1 = PLAYER_PVP.get(pl);
        PVP pvp2 = PLAYER_PVP.get(plMap);
        if (pvp1 == null && pvp2 == null) {
            PLAYER_VS_PLAYER.put(pl, plMap);
            NpcService.gI().createMenuConMeo(pl, ConstNpc.MAKE_MATCH_PVP,
                    -1,
                    plMap.name + " (sức mạnh " + Util.numberToMoney(plMap.nPoint.power)
                            + ")\nBạn muốn cược bao nhiêu vàng?",
                    this.optionsGoldChallenge);
        } else {
            Service.getInstance().hideWaitDialog(pl);
            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
        }
    }

    public void sendInvitePVP(Player pl, byte selectGold) {
        if (pl.getSession().actived) {
            Player plReceive = PLAYER_VS_PLAYER.get(pl);
            if (plReceive != null) {
                if (plReceive.getSession().actived) {
                    int gold = GOLD_CHALLENGE[selectGold];
                    if (pl.inventory.gold >= gold) {
                        if (plReceive.inventory.gold < gold) {
                            Service.getInstance().sendThongBao(pl,
                                    "Đối thủ chỉ có " + plReceive.inventory.gold + " vàng, không đủ tiền cược");
                        } else {
                            PLAYER_GOLD.put(pl, gold);
                            Message msg = null;
                            try {
                                msg = new Message(-59);
                                msg.writer().writeByte(3);
                                msg.writer().writeInt((int) pl.id);
                                msg.writer().writeInt(gold);
                                msg.writer().writeUTF(pl.name + " (sức mạnh " + Util.numberToMoney(pl.nPoint.power)
                                        + ") muốn thách đấu bạn với mức cược " + gold);
                                plReceive.sendMessage(msg);
                                msg.cleanup();
                            } catch (Exception e) {
                            }
                        }
                    } else {
                        Service.getInstance().sendThongBao(pl,
                                "Bạn chỉ có " + pl.inventory.gold + " vàng, không đủ tiền cược");
                    }
                } else {
                    Service.getInstance().sendThongBao(pl, "Đối thủ chưa kích hoạt tài khoản");
                }
            }
        } else {
            Service.getInstance().sendThongBaoFromAdmin(pl,
                    "|5|VUI LÒNG KÍCH HOẠT TÀI KHOẢN TẠI\n|7|" + Manager.DOMAIN
                            + "\n|5|ĐỂ MỞ KHÓA TÍNH NĂNG THÁCH ĐẤU");
        }
    }

    private void acceptPVP(Player pl) {
        if (pl.getSession().actived) {
            Player pl2 = PLAYER_VS_PLAYER.get(pl);
            if (pl2 != null) {
                PVP pvp1 = PLAYER_PVP.get(pl);
                PVP pvp2 = PLAYER_PVP.get(pl2);
                if (pvp1 == null && pvp2 == null) {
                    if (pl.zone.equals(pl2.zone)) {
                        int gold = PLAYER_GOLD.get(pl2);
                        if (pl.inventory.gold >= gold && pl2.inventory.gold >= gold) {
                            ChallengePVP pvp = new ChallengePVP(pl, pl2);
                            pvp.gold = gold;
                            PVPS.add(pvp);
                            PLAYER_PVP.put(pl, pvp);
                            PLAYER_PVP.put(pl2, pvp);
                            pvp.start();
                        }
                    } else {
                        Service.getInstance().sendThongBao(pl, "Đối thủ đã rời khỏi map");
                    }
                } else {
                    Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
                }
            }
        } else {
            Service.getInstance().sendThongBaoFromAdmin(pl,
                    "|5|VUI LÒNG KÍCH HOẠT TÀI KHOẢN TẠI\n|7|" + Manager.DOMAIN
                            + "\n|5|ĐỂ MỞ KHÓA TÍNH NĂNG THÁCH ĐẤU");
        }
    }

    public PVP findPvp(Player pl) {
        return PLAYER_PVP.get(pl);
    }

    public void removePVP(PVP pvp) {
        if (pvp != null) {
            Player pl1 = pvp.player1;
            Player pl2 = pvp.player2;
            if (pl1 != null) {
                PLAYER_VS_PLAYER.remove(pl1);
                PLAYER_GOLD.remove(pl1);
                PLAYER_PVP.remove(pl1);
            }
            if (pl2 != null) {
                PLAYER_VS_PLAYER.remove(pl2);
                PLAYER_GOLD.remove(pl2);
                PLAYER_PVP.remove(pl2);
            }
            PVPS.remove(pvp);
        }
    }

    public void finishPVP(Player plLose, byte typeLose) {
        PVP pvp = findPvp(plLose);
        if (pvp != null) {
            pvp.finishPVP(plLose, typeLose);
        }
    }

    public void openSelectRevenge(Player pl, Player enemy) {
        PVP pvp1 = findPvp(pl);
        PVP pvp2 = findPvp(enemy);
        if (pvp1 == null && pvp2 == null) {
            PLAYER_VS_PLAYER.put(pl, enemy);
            NpcService.gI().createMenuConMeo(pl, ConstNpc.REVENGE,
                    -1, "Bạn muốn đến ngay chỗ hắn, phí là 10 hồng ngọc và được tìm thoải mái trong 5 phút nhé", "Ok",
                    "Từ chối");
        } else {
            Service.getInstance().hideWaitDialog(pl);
            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
        }
    }

    public void acceptRevenge(Player pl) {
        if (pl.inventory.getRuby() > 10) {
            pl.inventory.subRuby(10);
            PlayerService.gI().sendInfoHpMpMoney(pl);
            Player enemy = PLAYER_VS_PLAYER.get(pl);
            if (enemy != null) {
                Zone mapGo = enemy.zone;
                if ((mapGo = ChangeMapService.gI().checkMapCanJoin(pl, mapGo)) != null && !mapGo.isFullPlayer()) {
                    RevengePVP pvp = new RevengePVP(pl, enemy);
                    PLAYER_PVP.put(pl, pvp);
                    PLAYER_PVP.put(enemy, pvp);
                    PVPS.add(pvp);
                    ChangeMapService.gI().changeMap(pl, mapGo,
                            enemy.location.x + Util.nextInt(-5, 5), enemy.location.y);
                    pvp.lastTimeGoToMapEnemy = System.currentTimeMillis();
                    pvp.start();
                } else {
                    Service.getInstance().sendThongBao(pl, "Không thể tới khu vực này, vui lòng đợi sau ít phút");
                }
            }
        } else {
            Service.getInstance().sendThongBao(pl, "Bạn không đủ ngọc, còn thiếu 10 hồng ngọc nữa");
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                long st = System.currentTimeMillis();
                for (PVP pvp : PVPS) {
                    pvp.update();
                }
                Thread.sleep(1000 - (System.currentTimeMillis() - st));
            } catch (Exception e) {
            }
        }
    }
}
