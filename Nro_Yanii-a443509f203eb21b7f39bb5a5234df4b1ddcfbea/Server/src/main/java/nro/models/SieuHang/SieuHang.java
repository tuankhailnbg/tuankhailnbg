///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package nro.models.SieuHang;
//
//import lombok.Getter;
//import lombok.Setter;
//import nro.consts.ConstItem;
//import nro.consts.ConstMap;
//import nro.consts.ConstPlayer;
//import nro.consts.ConstRewardLimit;
//import nro.event.Event;
//import nro.event.SummerEvent;
//import nro.models.boss.Boss;
//import nro.models.item.Item;
//import nro.models.player.Player;
//import nro.models.skill.Skill;
//import nro.services.EffectSkillService;
//import nro.services.InventoryService;
//import nro.services.ItemService;
//import nro.services.ItemTimeService;
//import nro.services.PlayerService;
//import nro.services.Service;
//import nro.services.func.ChangeMapService;
//import nro.utils.Util;
//
///**
// *
// * Author: louis
// */
//public class SieuHang {
//
//    @Setter
//    @Getter
//    private Player player;
//    @Setter
//    private Boss boss;
//    @Setter
//    private Player npc;
//
//    @Setter
//    private int time;
//    private int round;
//    @Setter
//    private int timeWait;
//
//    public void update() {
//        if (time > 0) {
//            time--;
//            if (player != null && !player.isDie() && player.zone != null) {
//                if (boss.isDie()) {
//                    round++;
//                    boss.leaveMap();
//                }
//                if (player.location.y > 264 && time > 10 && timeWait <= 0) {
//                    leave();
//                }
//            } else {
//                endChallenge();
//            }
//        } else {
//            timeOut();
//        }
//        if (timeWait > 0) {
//            switch (timeWait) {
//                case 10:
//                    prepareForBattle();
//                    break;
//                case 8:
//                    Service.getInstance().chat(npc, "Xin quý vị khán giả cho 1 tràng pháo tay để cổ vũ cho 2 đối thủ nào");
//                    break;
//                case 4:
//                    Service.getInstance().chat(npc, "Mọi người ngồi sau hãy ổn định chỗ ngồi, trận đấu sẽ bắt đầu sau 3 giây nữa");
//                    break;
//                case 2:
//                    Service.getInstance().chat(npc, "Trận đấu bắt đầu");
//                    PlayerService.gI().playerMove(npc, 385, 360);
//                    break;
//                case 1:
//                    Service.getInstance().chat(player, "Ok");
//                    Service.getInstance().chat(boss, "Ok");
//                    break;
//            }
//            
//        }
//    }
//
//    private void prepareForBattle() {
//        PlayerService.gI().setPos(player, 335, 264, 0);
//        PlayerService.gI().playerMove(npc, 385, 264);
//        Service.getInstance().chat(npc, "Trận đấu giữa " + player.name + " VS " + boss.name + " sắp diễn ra");
//        ready();
//    }
//
//    public void ready() {
//        if (player != null) {
//            EffectSkillService.gI().startStun(boss, System.currentTimeMillis(), 10000);
//            EffectSkillService.gI().startStun(player, System.currentTimeMillis(), 10000);
//            ItemTimeService.gI().sendItemTime(player, 3779, 10);
//            Service.getInstance().releaseCooldownSkill(player);
//            Util.setTimeout(() -> {
//                SieuHangService.gI().sendTypePK(player, boss);
//                PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.PK_PVP);
//                boss.setStatus((byte) 3);
//            }, 10000);
//        }
//    }
//
//    public void toTheNextRound(Boss bss) {
//        try {
//            PlayerService.gI().setPos(player, 335, 264, 0);
//            setTimeWait(11);
//            setBoss(bss);
//            System.out.println("OK");
//            setTime(185);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void resetSkill() {
//        if (player != null) {
//            for (Skill skill : player.playerSkill.skills) {
//                skill.lastTimeUseThisSkill = 0;
//            }
//            Service.getInstance().sendTimeSkill(player);
//        }
//    }
//
//    public Boss getBoss() {
//        return boss;
//    }
//
//    private void timeOut() {
//        if (player != null) {
//            Service.getInstance().sendThongBao(player, "Bạn bị xử thua vì hết thời gian");
//            endChallenge();
//        }
//    }
//
//    private void champion() {
//        if (player != null) {
//            Service.getInstance().sendThongBao(player, "Chúc mừng " + player.name + " vừa đoạt giải vô địch");
//            endChallenge();
//        }
//    }
//
//    public void leave() {
//        setTime(0);
//        if (player != null) {
//            EffectSkillService.gI().removeStun(player);
//            Service.getInstance().sendThongBao(player, "Bạn bị xử thua vì rời khỏi võ đài");
//            endChallenge();
//        }
//    }
//
//    private void reward() {
//        if (player != null && player.levelWoodChest < round) {
//            player.levelWoodChest = round;
//        }
//    }
//
//    public void endChallenge() {
//        if (round > 5 && Event.isEvent() && Event.getInstance() instanceof SummerEvent) {
//            byte[] rwLimit = player.getRewardLimit();
//            if (rwLimit[ConstRewardLimit.QUE_DOT] < 10) {
//                rwLimit[ConstRewardLimit.QUE_DOT]++;
//                Item item = ItemService.gI().createNewItem((short) ConstItem.QUE_DOT, 1);
//                InventoryService.gI().addItemBag(player, item, 99);
//            }
//        }
//        reward();
//        PlayerService.gI().hoiSinh(player);
//        PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
//        if (player != null && player.zone != null && player.zone.map.mapId == ConstMap.DAI_HOI_VO_THUAT_129) {
//            Util.setTimeout(() -> {
//                ChangeMapService.gI().changeMapNonSpaceship(player, ConstMap.DAI_HOI_VO_THUAT_129, player.location.x, 360);
//            }, 500);
//        }
//        if (boss != null) {
//            boss.leaveMap();
//        }
//        SieuHangManager.gI().remove(this);
//    }
//}
