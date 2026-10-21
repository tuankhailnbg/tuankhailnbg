/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.models.map.dungeon.zones;

import nro.consts.Cmd;
import nro.models.map.MapTemplate;
import nro.models.mob.MobTemplate;
import nro.models.map.Map;
import nro.models.map.WayPoint;
import nro.models.map.Zone;
import nro.models.map.dungeon.Dungeon;
import nro.models.mob.Mob;
import nro.models.player.Player;
import nro.server.Manager;
import nro.server.io.Message;
import nro.services.MapService;
import nro.services.Service;
import nro.services.func.ChangeMapService;
import lombok.Getter;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
@Getter
public abstract class ZDungeon extends Zone {

    protected Dungeon dungeon;

    public ZDungeon(Map map, Dungeon dungeon) {
        super(map, 0, 20);
        map.addZone(this);
        this.dungeon = dungeon;
        init();
    }

    public void init() {
        MapTemplate template = Manager.getMapTemplate(map.mapId);
        if (template != null) {
            for (int i = 0; i < template.mobTemp.length; i++) {
                MobTemplate temp = Manager.getMobTemplateByTemp(template.mobTemp[i]);
                Mob mob = new Mob();
                mob.tempId = template.mobTemp[i];
                mob.level = template.mobLevel[i];
                mob.point.setHpFull(template.mobHp[i]);
                mob.point.setHP(template.mobHp[i]);
                mob.location.x = template.mobX[i];
                mob.location.y = template.mobY[i];
                mob.pDame = temp.percentDame;
                mob.pTiemNang = temp.percentTiemNang;
                mob.setTiemNang();
                mob.status = 5;
                mob.zone = this;
                initMob(mob);
                addMob(mob);
            }
        }
    }

    public abstract void initMob(Mob mob);

    public void setTextTime() {
        Message msg;
        try {
            msg = new Message(Cmd.MESSAGE_TIME);
            msg.writer().writeByte(dungeon.getType());
            msg.writer().writeUTF(dungeon.getTitle());
            msg.writer().writeShort(dungeon.getCountDown());
            Service.getInstance().sendMessAllPlayerInMap(this, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void enter(Player player, int x, int y) {
        ChangeMapService.gI().changeMap(player, this, x, y);
        setTextTime();
    }

    @Override
    public void changeMapWaypoint(Player player) {
        WayPoint wp = MapService.gI().getWaypointPlayerIn(player);
        if (wp != null) {
            ZDungeon z = dungeon.find(wp.goMap);
            if (z != null) {
                int xGo = wp.goX;
                int yGo = wp.goY;
                z.enter(player, xGo, yGo);
            }
        }
    }

    public void close() {
        map.removeZone(this);
    }

}
