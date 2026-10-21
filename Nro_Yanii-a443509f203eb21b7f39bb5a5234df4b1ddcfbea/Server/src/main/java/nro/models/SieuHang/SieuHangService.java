//package nro.models.SieuHang;
//
//import java.util.ArrayList;
//import java.util.List;
//import nro.consts.ConstPlayer;
//import nro.jdbc.daos.GodGK;
//import nro.models.boss.Boss;
//import nro.models.boss.BossData;
//import static nro.models.boss.BossData._0_GIAY;
//import nro.models.map.Zone;
//import nro.models.player.Player;
//import nro.models.skill.Skill;
//import nro.server.io.Message;
//import nro.services.MapService;
//import nro.services.Service;
//import nro.services.func.ChangeMapService;
//import nro.utils.Util;
//
///**
// *
// * @author louis
// */
//public class SieuHangService {
//
//    private static SieuHangService i;
//
//    public static SieuHangService gI() {
//        if (i == null) {
//            i = new SieuHangService();
//        }
//        return i;
//    }
//
//    public boolean checkDangThiDau(Player player1, Player player2) {
//        for (SieuHang sieuHang : SieuHangManager.gI().list) {
//            if (player1.name.equals(sieuHang.getPlayer().name) || player1.name.equals(sieuHang.getBoss().name) || player2.name.equals(sieuHang.getPlayer().name) || player2.name.equals(sieuHang.getBoss().name)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public void startChallenge(Player player, int id) throws Exception {
//        if (player.zone.map.mapId != 113) {
//            return;
//        }
//        Player pl = GodGK.loadByID(id);
//        if (pl != null) {
//            pl.nPoint.calPoint();
//            if (Math.abs(player.rankSieuHang - pl.rankSieuHang) > 2) {
//                Service.gI().sendThongBao(player, "Không thể thách đấu đối thủ cách quá 2 hạng");
//                return;
//            }
//            if (checkDangThiDau(player, pl)) {
//                Service.gI().sendThongBao(player, "Đối thủ đang thi đấu");
//                return;
//            }
//
////            int[][] skillTemp = new int[pl.playerSkill.skills.size()][3];
////            for (byte i = 0; i < pl.playerSkill.skills.size(); i++) {
////                Skill skill = pl.playerSkill.skills.get(i);
////                if (skill.point > 0) {
////                    skillTemp[i][0] = skill.template.id;
////                    skillTemp[i][1] = skill.point;
////                    skillTemp[i][2] = skill.IsSkillDam(skill.template.id) ? 300 : skill.coolDown;
////                }
////            }
////            BossData data = BossData.builder()
////                    .name(pl.name)
////                    .gender(pl.gender)
////                    .dame(pl.nPoint.dame)
////                    .hp(new int[]{pl.nPoint.hpMax})
////                    .outfit(new short[]{pl.getHead(), pl.getBody(), pl.getLeg(), -1, -1, -1})
////                    .mapJoin(new int[]{113})
////                    .skillTemp(skillTemp)
////                    .secondsRest(5)
////                    .build();
            
//
//            BossData data = new BossData(
//                    pl.name, //name
//                    pl.gender, //gender
//                    Boss.DAME_NORMAL, //type dame
//                    Boss.HP_NORMAL, //type hp
//                    pl.nPoint.dame, //dame
//                    new int[][]{{pl.nPoint.hpMax}}, //hp
//                    new short[]{pl.getHead(),pl.getBody(),pl.getLeg()}, //outfit
//                    new short[]{}, //map join
//                    skillTemp,
//                    _0_GIAY
//            );
//
//            ClonePlayer boss = new ClonePlayer(player, data, (int) pl.id);
//            
//            boss.rankSieuHang = pl.rankSieuHang;
//            Zone zone = getMapChalllenge(113);
//            if (zone != null) {
//                ChangeMapService.gI().changeMap(player, zone, player.location.x, 360);
//                Util.setTimeout(() -> {
//                    SieuHang mc = new SieuHang();
//                    mc.setPlayer(player);
//                    mc.toTheNextRound(boss);
//                    SieuHangManager.gI().add(mc);
//                }, 500);
//            }
//        }
//    }
//
//    public void moveFast(Player pl, int x, int y) {
//        Message msg;
//        try {
//            msg = new Message(58);
//            msg.writer().writeInt((int) pl.id);
//            msg.writer().writeShort(x);
//            msg.writer().writeShort(y);
//            msg.writer().writeInt((int) pl.id);
//            pl.sendMessage(msg);
//            msg.cleanup();
//        } catch (Exception e) {
//        }
//    }
//
//    public void sendTypePK(Player player, Player boss) {
//        Message msg;
//        try {
//            msg = Service.getInstance().messageSubCommand((byte) 35);
//            msg.writer().writeInt((int) boss.id);
//            msg.writer().writeByte(3);
//            player.sendMessage(msg);
//            msg.cleanup();
//        } catch (Exception e) {
//        }
//    }
//
//    public Zone getMapChalllenge(int mapId) {
//        Zone map = MapService.gI().getMapWithRandZone(mapId);
//        if (map.getNumOfBosses() < 1) {
//            return map;
//        }
//        return null;
//    }
//}
