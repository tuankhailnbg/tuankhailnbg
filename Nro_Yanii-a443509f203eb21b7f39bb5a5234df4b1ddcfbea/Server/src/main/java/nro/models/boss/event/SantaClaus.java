/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.models.boss.event;

import nro.consts.ConstMap;
import nro.consts.ConstPlayer;
import nro.consts.ConstRatio;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.services.PlayerService;
import nro.services.Service;
import nro.services.func.ChangeMapService;
import nro.utils.Util;
import org.apache.log4j.Logger;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
public class SantaClaus extends Boss {
    private static final Logger logger = Logger.getLogger(SantaClaus.class);

    private int mapID;
    private long lastTimeRespawn;
    private long lastTimeDropItem;

    public SantaClaus(int mapID) {
        super(BossFactory.SANTA_CLAUS, BossData.SANTA_CLAUS);
        this.mapID = mapID;
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

    @Override
    public void rewards(Player pl) {

    }

    @Override
    public void idle() {

    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    public void initTalk() {
        textTalkBefore = new String[] { "Giáng sinh vui vẻ" };
        textTalkMidle = new String[] { "Hô hô hô", "Giáng sinh vui vẻ", "Giáng sinh an lành" };
        textTalkAfter = new String[] { "Bye bye" };
    }

    @Override
    public void changeToAttack() {
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.NON_PK);
        changeStatus(ATTACK);
    }

    @Override
    public void joinMap() {
        this.zone = getMapCanJoin(mapID);
        int x = Util.nextInt(50, this.zone.map.mapWidth - 50);
        ChangeMapService.gI().changeMap(this, this.zone, x, this.zone.map.yPhysicInTop(x, 0));
    }

    @Override
    public void attack() {
        if (Util.isTrue(50, ConstRatio.PER100)) {
            int x = location.x + Util.nextInt(-50, 50);
            if (x < 35) {
                x = 35;
            } else if (x > this.zone.map.mapWidth - 35) {
                x = this.zone.map.mapWidth - 35;
            }
            int y = location.y;
            if (location.y > 50) {
                y = this.zone.map.yPhysicInTop(x, y - 50);
            }
            goToXY(x, y, false);
        }
        if (Util.canDoWithTime(lastTimeRespawn, 90000)) {
            changeStatus(TALK_AFTER);
        } else {
            if (this.zone.getNumOfPlayers() > 0) {
                if (Util.canDoWithTime(lastTimeDropItem, 15000)) {
                    int n = Util.nextInt(2, 4);
                    for (int i = 0; i < n; i++) {
                        dropItemReward(648, -1);
                    }
                    lastTimeDropItem = System.currentTimeMillis();
                }
            }
        }
    }

    @Override
    public void dropItemReward(int tempId, int playerId, int... quantity) {
        try {
            if (!this.zone.map.isMapOffline && this.zone.map.type == ConstMap.MAP_NORMAL) {
                int x = this.location.x + Util.nextInt(-30, 30);
                if (x < 30) {
                    x = 30;
                } else if (x > zone.map.mapWidth - 30) {
                    x = zone.map.mapWidth - 30;
                }
                int y = this.location.y;
                if (y > 24) {
                    y = this.zone.map.yPhysicInTop(x, y - 24);
                }
                ItemMap itemMap = new ItemMap(this.zone, tempId,
                        (quantity != null && quantity.length == 1) ? quantity[0] : 1, x, y, playerId);
                Service.getInstance().dropItemMap(itemMap.zone, itemMap);
            }
        } catch (Exception e) {
            logger.error("Err drop item boss santa", e);
        }
    }

    @Override
    public boolean talk() {
        switch (status) {
            case TALK_BEFORE:
                if (this.textTalkBefore == null || this.textTalkBefore.length == 0) {
                    return true;
                }
                if (Util.canDoWithTime(lastTimeTalk, 3000)) {
                    if (indexTalkBefore < textTalkBefore.length) {
                        this.chat(textTalkBefore[indexTalkBefore++]);
                        if (indexTalkBefore >= textTalkBefore.length) {
                            return true;
                        }
                        lastTimeTalk = System.currentTimeMillis();
                    } else {
                        return true;
                    }
                }
                break;
            case IDLE:
            case ATTACK:
                if (this.textTalkMidle == null || this.textTalkMidle.length == 0) {
                    return true;
                }
                if (Util.canDoWithTime(lastTimeTalk, Util.nextInt(5000, 8000))) {
                    this.chat(textTalkMidle[Util.nextInt(0, textTalkMidle.length - 1)]);
                    lastTimeTalk = System.currentTimeMillis();
                }
                break;
            case TALK_AFTER:
                if (this.textTalkAfter == null || this.textTalkAfter.length == 0) {
                    return true;
                }
                if (Util.canDoWithTime(lastTimeTalk, 2000)) {
                    this.chat(textTalkAfter[indexTalkAfter++]);
                    if (indexTalkAfter >= textTalkAfter.length) {
                        return true;
                    }
                    if (indexTalkAfter > textTalkAfter.length - 1) {
                        indexTalkAfter = 0;
                    }
                    lastTimeTalk = System.currentTimeMillis();
                }
                break;
        }
        return false;
    }

    @Override
    public void respawn() {
        super.respawn();
        lastTimeRespawn = lastTimeDropItem = System.currentTimeMillis();
    }

}
