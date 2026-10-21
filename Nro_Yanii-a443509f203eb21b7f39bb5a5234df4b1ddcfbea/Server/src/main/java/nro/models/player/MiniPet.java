package nro.models.player;

import lombok.Setter;
import nro.manager.MiniPetManager;
import nro.models.item.MinipetTemplate;
import nro.services.MapService;
import nro.services.Service;
import nro.utils.Log;
import nro.utils.Util;
import nro.services.PlayerService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
/**
 * @author outcast c-cute hột me 😳
 */
public class MiniPet extends Player {
private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public static final byte FOLLOW = 0;

    public static final byte GOHOME = 1;

    public Player master;

    public byte status = 0;

    @Setter
    private short head, body, leg;

    public static void callMiniPet(Player pl, int itemID) {
        MiniPet miniPet = pl.minipet;
        if (miniPet == null) {
            miniPet = new MiniPet(pl, itemID);
        } else {
            miniPet.changeStatus(FOLLOW);
        }
        miniPet.setPart(itemID);
        Service.getInstance().point(pl);
    }

    public void setPart(int id) {
        MinipetTemplate temp = MiniPetManager.gI().findByID(id);
        if (temp != null) {
            setHead(temp.getHead());
            setBody(temp.getBody());
            setLeg(temp.getLeg());
        }
    }

    @Override
    public int version() {
        return 214;
    }

    @Override
    public short getHead() {
        return head;
    }

    @Override
    public short getBody() {
        return body;
    }

    @Override
    public short getLeg() {
        return leg;
    }

    public MiniPet(Player player, int tempID) {
        this.master = player;
        this.isMiniPet = true;
        this.name = "# ";
        this.gender = 0;
        this.id = master.id - 1000000;
        this.nPoint.hpMax = 5000000;
        this.nPoint.hpg = 5000000;
        this.nPoint.hp = 5000000;
        this.nPoint.setFullHpMp();
        master.minipet = this;
    }

    public void changeStatus(byte status) {
        if (master.minipet != null) {
            reCall();
        }
        this.status = status;
    }

    public void reCall() {
        MapService.gI().goToMap(this, MapService.gI().getMapCanJoin(this, master.gender + 21));
        this.zone.load_Me_To_Another(this);
    }

    public void joinMapMaster() {
        if (master.zone != null) {
            if (!MapService.gI().isMapVS(master.zone.map.mapId)) {
                this.location.x = master.location.x + Util.nextInt(-10, 10);
                this.location.y = master.location.y;
                MapService.gI().goToMap(this, master.zone);
                this.zone.load_Me_To_Another(this);
            }
        }
    }

    public long lastTimeMoveIdle;
    private int timeMoveIdle;
    public boolean idle;

    private void moveIdle() {
        if (status == GOHOME) {
            return;
        }
        if (idle && Util.canDoWithTime(lastTimeMoveIdle, timeMoveIdle)) {
            int dir = this.location.x - master.location.x <= 0 ? -1 : 1;
            PlayerService.gI().playerMove(this, master.location.x
                    + Util.nextInt(dir == -1 ? 30 : -50, dir == -1 ? 50 : 30), master.location.y);
            lastTimeMoveIdle = System.currentTimeMillis();
            timeMoveIdle = Util.nextInt(5000, 8000);
        }
    }

    @Override
    public void update() {
        try {
            super.update();
            if (this.zone == null || this.zone != master.zone) {
                joinMapMaster();
            }
            moveIdle();
            switch (status) {
                case FOLLOW:
                    break;
            }
            if (!master.inventory.itemsBody.get(11).isNotNullItem()) {
                MapService.gI().exitMap(this);
                this.dispose();
                master.minipet = null;
            }
        } catch (Exception e) {
            Log.error(MiniPet.class, e);
        }
    }

    public void followMaster() {
        switch (this.status) {
            case FOLLOW:
                followMaster(80);
                break;
        }
    }

    private void followMaster(int dis) {
        int mX = master.location.x;
        int mY = master.location.y;
        int disX = this.location.x - mX;
        if (Math.sqrt(Math.pow(mX - this.location.x, 2) + Math.pow(mY - this.location.y, 2)) >= dis) {
            if (disX < 0) {
                this.location.x = mX - Util.nextInt(dis - 1, dis);
            } else {
                this.location.x = mX + Util.nextInt(dis - 1, dis);
            }
            this.location.y = mY;
            PlayerService.gI().playerMove(this, this.location.x, this.location.y);
        }
    }
}
