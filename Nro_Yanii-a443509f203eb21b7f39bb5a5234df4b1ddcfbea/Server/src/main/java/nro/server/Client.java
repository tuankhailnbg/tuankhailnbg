package nro.server;

import nro.login.LoginSession;
import nro.models.item.Item;
import nro.models.map.war.NamekBallWar;
import nro.models.player.Player;
import nro.models.pvp.PVP;
import nro.server.io.Session;
import nro.services.InventoryService;
import nro.services.ItemTimeService;
import nro.services.MapService;
import nro.services.PlayerService;
import nro.services.Service;
import nro.services.func.PVPServcice;
import nro.services.func.SummonDragon;
import nro.services.func.TransactionService;
import nro.utils.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class Client implements Runnable {

    private static Client i;

    @Getter
    public final List<Session> sessions = new ArrayList<>();
    private final Map<Integer, Session> sessions_id = new HashMap<Integer, Session>();
    private final Map<Long, Player> players_id = new HashMap<Long, Player>();
    private final Map<Integer, Player> players_userId = new HashMap<Integer, Player>();
    private final Map<String, Player> players_name = new HashMap<String, Player>();
    private final List<Player> players = new ArrayList<>();

    private boolean running = true;

    private Client() {
        new Thread(this).start();
    }

    public List<Player> getPlayers() {
        synchronized (players) {
            return this.players.stream().collect(Collectors.toList());
        }
    }

    public static Client gI() {
        if (i == null) {
            i = new Client();
        }
        return i;
    }

    public void put(Session session) {
        synchronized (sessions) {
            if (!sessions_id.containsValue(session)) {
                this.sessions_id.put(session.id, session);
            }
            if (!sessions.contains(session)) {
                this.sessions.add(session);
            }
        }
    }

    public void put(Player player) {
        if (!players_id.containsKey(player.id)) {
            this.players_id.put(player.id, player);
        }
        if (!players_name.containsValue(player)) {
            this.players_name.put(player.name, player);
        }
        if (!players_userId.containsValue(player)) {
            this.players_userId.put(player.getSession().userId, player);
        }
        if (!players.contains(player)) {
            this.players.add(player);
        }

    }

    private void remove(Session session) {
        synchronized (sessions) {
            this.sessions_id.remove(session.id);
            this.sessions.remove(session);
            LoginSession login = ServerManager.gI().getLogin();
            if (login != null && login.isConnected()) {
                login.getService().logout(session.userId);
            }
            if (session.player != null) {
                this.remove(session.player);
                session.player.dispose();
            }
            if (session.loginSuccess && session.joinedGame) {
                session.loginSuccess = false;
                session.joinedGame = false;
                // AccountDAO.updateAccoutLogout(session);
            }
            ServerManager.gI().disconnect(session);
        }
    }

    private void remove(Player player) {
        this.players_id.remove(player.id);
        this.players_name.remove(player.name);
        this.players_userId.remove(player.getSession().userId);
        this.players.remove(player);
        dispose(player);
    }

    public void dispose(Player player) {
        if (!player.beforeDispose) {
            if (player.isHoldNamecBall) {
                NamekBallWar.gI().dropBall(player);
            }
            player.beforeDispose = true;
            player.mapIdBeforeLogout = player.zone.map.mapId;
            MapService.gI().exitMap(player);
            TransactionService.gI().cancelTrade(player);
            PVPServcice.gI().finishPVP(player, PVP.TYPE_LEAVE_MAP);
            if (player.clan != null) {
                player.clan.removeMemberOnline(null, player);
            }
            if (player.itemTime != null && player.itemTime.isUseTDLT) {
                Item tdlt = InventoryService.gI().findItemBagByTemp(player, 521);
                if (tdlt != null) {
                    ItemTimeService.gI().turnOffTDLT(player, tdlt);
                }
            }
            if (SummonDragon.gI().playerSummonShenron != null
                    && SummonDragon.gI().playerSummonShenron.id == player.id) {
                SummonDragon.gI().isPlayerDisconnect = true;
            }
            if (player.mobMe != null) {
                player.mobMe.mobMeDie();
            }
            if (player.pet != null) {
                if (player.pet.mobMe != null) {
                    player.pet.mobMe.mobMeDie();
                }
                MapService.gI().exitMap(player.pet);
            }
            if (player.minipet != null) {
                MapService.gI().exitMap(player.minipet);
            }
            PlayerService.gI().savePlayer(player);
        }
    }

    public void kickSession(Session session) {
        if (session != null) {
            this.remove(session);
            session.disconnect();
        }
    }

    public Player getPlayer(long playerId) {
        return this.players_id.get(playerId);
    }

    public Player getPlayerByUser(int userId) {
        return this.players_userId.get(userId);
    }

    public Session getSession(Session session) {
        synchronized (sessions) {
            for (Session se : sessions) {
                if (se != session && se.userId == session.userId) {
                    return se;
                }
            }
        }
        return null;
    }

    public Player getPlayer(String name) {
        return this.players_name.get(name);
    }

    public Session getSession(int sessionId) {
        return this.sessions_id.get(sessionId);
    }

    public void close() {
        Log.log("BEGIN KICK OUT SESSION...............................");
        synchronized (sessions) {
            while (!this.sessions.isEmpty()) {
                Log.log("LEFT PLAYER: " + this.players.size() + ".........................");
                this.kickSession(this.sessions.remove(0));
            }
        }
    }

    private void update() {
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.timeWait > 0) {
                    session.timeWait--;
                    if (session.timeWait == 0) {
                        kickSession(session);
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        while (ServerManager.isRunning) {
            try {
                long st = System.currentTimeMillis();
                update();
                Thread.sleep(800 - (System.currentTimeMillis() - st));
            } catch (Exception e) {
            }
        }
    }

    public void show(Player player) {
        String txt = "";
        txt += "sessions: " + sessions.size() + "\n";
        txt += "sessions_id: " + sessions_id.size() + "\n";
        txt += "players_id: " + players_id.size() + "\n";
        txt += "players_userId: " + players_userId.size() + "\n";
        txt += "players_name: " + players_name.size() + "\n";
        txt += "players: " + players.size() + "\n";
        Service.getInstance().sendThongBao(player, txt);
    }
}
