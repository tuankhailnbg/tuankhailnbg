package nro.server.io;

import nro.data.DataGame;
import nro.jdbc.daos.GodGK;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.player.Player;
import nro.resources.Resources;
import nro.server.*;
import nro.server.*;
import nro.server.model.AntiLogin;
import nro.services.*;
import nro.utils.Log;
import nro.utils.Util;
import lombok.Setter;
import nro.services.ItemService;
import nro.services.ItemTimeService;
import nro.services.Service;
import nro.services.TaskService;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static nro.server.Manager.KEY_SERVER;

public class Session {

    private static final Map<String, AntiLogin> ANTILOGIN = new HashMap<>();

    private static final int TIME_WAIT_READ_MESSAGE = 180000;

    public boolean logCheck;

    private static int baseId = 0;
    public int id;
    public Player player;

    public boolean isLock = false;

    public byte timeWait = 50;

    public boolean connected;

    static final byte[] KEYS = {0};
    byte curR, curW;
 public int vnd;
    private Socket socket;
    Thread sendThread;
    Thread receiveThread;
    Thread doControllerThread;
    private MessageCollector collector;
    private MessageSender sender;

    Controller controller;

    public String ipAddress;
    public boolean isAdmin;
    public int userId;
    public String uu;
    public String pp;

    public int typeClient;

    public String keyLockMod;

    public byte zoomLevel;
    public boolean isSetClientType;

    public long lastTimeLogout;
    public boolean loginSuccess, joinedGame, dataLoadFailed;

    public long lastTimeReadMessage;

    public boolean actived;

    public int goldBar;

    public int vndBar;

    public int tongnap;

    public int isUseMaBaoVe;

    public int MaBaoVe;

    public List<Item> itemsReward;
    public String dataReward;
    public int ruby;
    public int diemTichNap;
    public int server;// server account hiện tại
    public int version;
    @Setter
    private boolean logging;

    public void update() {
        if (Util.canDoWithTime(lastTimeReadMessage, TIME_WAIT_READ_MESSAGE)) {
//            Client.gI().kickSession(this);
        }
    }

    public void initItemsReward() {
        // demo và giải thích //

//        100:3|1:10,1:10;200:2|2:5;457:1|3:8
        // end //
        try {
            this.itemsReward = new ArrayList<>();
            String[] itemsReward = dataReward.split(";");
            for (String itemInfo : itemsReward) {
                if (itemInfo == null || itemInfo.equals("")) {
                    continue;
                }
                String[] subItemInfo = itemInfo.replaceAll("[{}\\[\\]]", "").split("\\|");
                String[] baseInfo = subItemInfo[0].split(":");
                int itemId = Integer.parseInt(baseInfo[0]);
                int quantity = Integer.parseInt(baseInfo[1]);
                Item item = ItemService.gI().createNewItem((short) itemId, quantity);
                if (subItemInfo.length == 2) {
                    String[] options = subItemInfo[1].split(",");
                    for (String opt : options) {
                        if (opt == null || opt.equals("")) {
                            continue;
                        }
                        String[] optInfo = opt.split(":");
                        int tempIdOption = Integer.parseInt(optInfo[0]);
                        int param = Integer.parseInt(optInfo[1]);
                        item.itemOptions.add(new ItemOption(tempIdOption, param));
                    }
                }
                this.itemsReward.add(item);
            }
        } catch (Exception e) {

        }
    }

    public int getNumOfMessages() {
        return this.sender.getNumMessage();
    }

    public Session(Socket socket, Controller controller, String ip) {
        try {
            socket.setTcpNoDelay(true);
            this.id = baseId++;
            this.socket = socket;
            this.controller = controller;
            this.sendThread = new Thread((this.sender = new MessageSender(this, socket)), "Send " + ip);
            this.receiveThread = new Thread((this.collector = new MessageCollector(this, socket)), "Receive " + ip);
//            this.doControllerThread = new Thread((this.doController = new MessageDoController(this)), "Do controller " + ip);
//            this.doControllerThread.start();
            this.receiveThread.start();
            Client.gI().put(this);
        } catch (Exception e) {
            Log.error(Session.class, e);
        }
    }

    public void sendMessage(Message msg) {
        if (this.sender != null) {
            sender.addMessage(msg);
        }
    }

    public void doSendMessage(Message msg) {
        if (this.sender != null) {
            sender.doSendMessage(msg);
        }
    }

    public void disconnect() {
        if (connected) {
            connected = false;
            curR = 0;
            curW = 0;
            this.player = null;
            try {
                if (this.sender != null) {
                    this.sender.close();
                }
                if (this.collector != null) {
                    this.collector.close();
                }
                if (socket != null) {
                    socket.close();
                }
                this.socket = null;
                this.sender = null;
                this.collector = null;
                this.sendThread = null;
                this.receiveThread = null;
                this.uu = null;
                this.pp = null;
                this.itemsReward = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setClientType(Session session, Message msg, int typeClient, byte zoomLevel, int version) {
        try {
            if (!isSetClientType) {
                session.typeClient = typeClient;//client_type
                session.zoomLevel = zoomLevel;//zoom_level
                session.version = version;
                isSetClientType = true;
                Resources.getInstance().sendResVersion(this);
            }
        } catch (Exception e) {
        } finally {
            msg.cleanup();
        }
        DataGame.sendLinkIP(session);
    }

    public void setClientType(Message msg) {
        try {
            if (!isSetClientType) {
                this.typeClient = (msg.reader().readByte());//client_type
                this.zoomLevel = msg.reader().readByte();//zoom_level
                msg.reader().readBoolean();//is_gprs
                msg.reader().readInt();//width
                msg.reader().readInt();//height
                msg.reader().readBoolean();//is_qwerty
                msg.reader().readBoolean();//is_touch
                String platform = msg.reader().readUTF();
                String[] arrPlatform = platform.split("\\|");
                this.version = Integer.parseInt(arrPlatform[1].replaceAll("\\.", ""));
                isSetClientType = true;
                Resources.getInstance().sendResVersion(this);
            }
        } catch (Exception e) {
        } finally {
            msg.cleanup();
        }
        DataGame.sendLinkIP(this);
    }

    public boolean isVersionAbove(int version) {
        return this.version >= version;
    }

    public String getName() {
        if (this.player != null) {
            return this.player.name;
        } else {
            return String.valueOf(this.socket.getPort());
        }
    }

    public void sendSessionKey() {
        this.sender.sendSessionKey();
    }

    public boolean canConnectWithIp() {
        Object o = ServerManager.CLIENTS.get(ipAddress);
        if (o == null) {
            ServerManager.CLIENTS.put(ipAddress, 1);
            return true;
        } else {
            int n = Integer.parseInt(String.valueOf(o));
            if (n < Manager.MAX_PER_IP) {
                n++;
                ServerManager.CLIENTS.put(ipAddress, n);
                return true;
            } else {
                return false;
            }
        }
    }

    public void login(String username, String password) {
        if (Maintenance.isRuning || !ServerManager.gI().getLogin().isConnected()) {
            Service.getInstance().sendThongBaoOK(this, "Máy chủ đang tiến hành bảo trì, vui lòng thử lại sau!");
            return;
        }
        if (!isSetClientType || logging || loginSuccess) {
            return;
        }
        logging = true;
        AntiLogin al = ANTILOGIN.get(this.ipAddress);
        if (al == null) {
            al = new AntiLogin();
            ANTILOGIN.put(this.ipAddress, al);
        }
        if (!al.canLogin()) {
            Service.getInstance().sendThongBaoOK(this, al.getNotifyCannotLogin());
            return;
        }
        if (!this.isAdmin && Client.gI().getPlayers().size() >= Manager.MAX_PLAYER) {
            Service.getInstance().sendThongBaoOK(this, "Máy chủ hiện đang quá tải, "
                    + "cư dân vui lòng di chuyển sang máy chủ khác.");
            return;
        }
        if (this.player != null) {
            return;
        } else {
            Player player = null;
            try {
                long st = System.currentTimeMillis();
                this.uu = username;
                this.pp = password;
                ServerManager.gI().getLogin().getService().login(Manager.SERVER, this.id, username, password);
            } catch (Exception e) {
                e.printStackTrace();
                if (player != null) {
                    player.dispose();
                }
            }
        }
    }

    public void finishUpdate() {
        if (loginSuccess && !joinedGame) {
            player = GodGK.loadPlayer(this);
            if (!dataLoadFailed) {
                if (player != null) {
                    enter();
                } else {
                    Service.getInstance().switchToCreateChar(this);
                }
            } else {
                Service.getInstance().sendThongBaoOK(this, "Lỗi tải dữ liệu vui lòng báo với admin.");
            }
        }
    }

    public void enter() {
        if (!joinedGame) {
            joinedGame = true;
            player.nPoint.initPowerLimit();
            if (player.pet != null) {
                player.pet.nPoint.initPowerLimit();
            }
            player.nPoint.calPoint();
            player.nPoint.setHp((int) player.nPoint.hp);
            player.nPoint.setMp((int) player.nPoint.mp);
            player.zone.addPlayer(player);
            player.loaded = true;
            if (player.pet != null) {
                player.pet.nPoint.calPoint();
                player.pet.nPoint.setHp(player.pet.nPoint.hp);
                player.pet.nPoint.setMp(player.pet.nPoint.mp);
            }

            player.setSession(this);
            Client.gI().put(player);
            controller.sendInfo(this);
            Service.getInstance().player(player);
            Service.getInstance().Send_Caitrang(player);

            // -64 my flag bag
            Service.getInstance().sendFlagBag(player);

            // -113 skill shortcut
            player.playerSkill.sendSkillShortCut();
            // item time
            ItemTimeService.gI().sendAllItemTime(player);

            // send current task
            TaskService.gI().sendInfoCurrentTask(player);

            // nhận quà đăng nhập hàng ngày
//            if (Manager.EVENT_SEVER == 3) {
//                RewardService.gI().rewardFirstTimeLoginPerDay(player);
//            }
        }
    }
}
