package nro.dialog;

import lombok.NoArgsConstructor;
import nro.consts.ConstNpc;
import nro.models.player.Player;
import nro.server.io.Message;
import nro.services.NpcService;
import nro.utils.Log;

import java.io.DataOutputStream;

/**
 * @author outcast c-cute hột me 😳
 */

@NoArgsConstructor
public class ConfirmDialog {

    protected String content;
    private Runnable run;
    private Runnable cancel;

    public ConfirmDialog(String content, Runnable run) {
        this.run = run;
        this.content = content;
    }

    public ConfirmDialog(String content, Runnable run, Runnable cancel) {
        this.content = content;
        this.run = run;
        this.cancel = cancel;
    }

    public void show(Player player) {
        player.iDMark.setIndexMenu(ConstNpc.CONFIRM_DIALOG);
        player.setConfirmDialog(this);
        Message msg = new Message(32);
        DataOutputStream ds = msg.writer();
        try {
            ds.writeShort(ConstNpc.CON_MEO);
            ds.writeUTF(content);
            ds.writeByte(2);
            ds.writeUTF("Đồng ý");
            ds.writeUTF("Từ chối");
            ds.flush();
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(NpcService.class, e);
        }
    }

    public void run() {
        run.run();
    }

    public void cancel() {
        cancel.run();
    }
}
