package nro.dialog;

import lombok.Getter;
import nro.consts.ConstNpc;
import nro.models.player.Player;
import nro.server.io.Message;
import nro.services.NpcService;
import nro.utils.Log;

import java.io.DataOutputStream;

/**
 * @author outcast c-cute hột me 😳
 */
public class MenuDialog extends ConfirmDialog{

    public String[] menu;

    @Getter
    private MenuRunable runable;

    public MenuDialog(String content, String[] menu, MenuRunable run) {
        super();
        this.content = content;
        this.menu = menu;
        this.runable = run;
    }

    @Override
    public void show(Player player) {
        player.iDMark.setIndexMenu(ConstNpc.CONFIRM_DIALOG);
        player.setConfirmDialog(this);
        Message msg = new Message(32);
        DataOutputStream ds = msg.writer();
        try {
            ds.writeShort(ConstNpc.CON_MEO);
            ds.writeUTF(content);
            ds.writeByte(menu.length);
            for (String str : menu) {
                ds.writeUTF(str);
            }
            ds.flush();
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(NpcService.class, e);
        }
    }

    @Override
    public void run() {
        runable.run();
    }
}
