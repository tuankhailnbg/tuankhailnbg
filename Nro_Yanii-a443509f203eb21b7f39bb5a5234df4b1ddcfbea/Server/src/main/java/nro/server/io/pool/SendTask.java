package nro.server.io.pool;

import nro.server.io.Session;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class SendTask implements Runnable {

    public static boolean check = false;

    private Session session;

    public SendTask(Session session) {
        this.session = session;
    }

    @Override
    public void run() {
        // if (!session.messages.isEmpty()) {
        // long st = System.currentTimeMillis();
        // for (int i = session.messages.size() - 1; i >= 0; i--) {
        // Message msg = session.messages.get(i);
        // if (msg != null) {
        // session.doSendMessage(msg);
        // }
        // session.messages.remove(i);
        // }
        // if(check && session.player != null &&
        // session.player.name.equals("girlkun75")){
        // System.out.println("********************************************time send to
        // me: " + (System.currentTimeMillis()));
        // }
        // }
    }

}
