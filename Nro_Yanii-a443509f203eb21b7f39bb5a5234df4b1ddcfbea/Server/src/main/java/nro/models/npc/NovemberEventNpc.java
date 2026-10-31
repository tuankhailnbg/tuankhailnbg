package nro.models.npc;

import nro.event.november.*;
import nro.models.player.Player;
import nro.services.InventoryService;
import nro.services.NovemberEventService;
import nro.services.Service;

public final class NovemberEventNpc extends Npc {
    private static final int MAIN=261100,CRAFT=261101,DONATE=261102,BOX=261103,
            SHOP=261104,DAILY=261105,TOP=261106,MODEL=261110,TOP_PICK=261120;
    public NovemberEventNpc(int map,int status,int x,int y,int id,int avatar) { super(map,status,x,y,id,avatar); }
    private NovemberEventService event() { return NovemberEventService.gI(); }
    @Override public void openBaseMenu(Player p) {
        if (!canOpenNpc(p) || mapId!=5) return;
        createOtherMenu(p,MAIN,event().status(p),"Ghép\nThiệp","Tặng\nThiệp","Hộp quà","Đổi\nCải trang",
                "Nhiệm vụ\nngày","Đua Top","Hướng dẫn","Đóng");
    }
    private void result(Player p,String text) {
        InventoryService.gI().sendItemBags(p); Service.getInstance().sendThongBaoOK(p,text); openBaseMenu(p);
    }
    private void quantities(Player p,int menu,String text) {
        createOtherMenu(p,menu,text,"1","10","100","Tối đa\n(1.000)","Quay lại");
    }
    @Override public void confirmMenu(Player p,int select) {
        if (!canOpenNpc(p) || mapId!=5 || select<0) return;
        int menu=p.iDMark.getIndexMenu();
        if (menu==MAIN) {
            switch(select) {
                case 0 -> quantities(p,CRAFT,"10 Bút + 5 Thiệp trắng = 1 Thiệp tri ân.");
                case 1 -> quantities(p,DONATE,"Mỗi thiệp tặng: +10 xu và +10 điểm. Điểm không bị trừ khi đổi quà.");
                case 2 -> createOtherMenu(p,BOX,"Hộp tự ghép phải mở trong tháng 11 để tính điểm. Hộp thưởng mở đến hết 03/12.",
                        "Ghép\n1 hộp","Ghép\n10 hộp","Mở hộp\ntự ghép","Mở hộp\nthưởng","Quay lại");
                case 3 -> createOtherMenu(p,SHOP,"Chọn hướng chơi. Mỗi cải trang: 50/150/500/1.500 xu cho 3/7/30 ngày/vĩnh viễn.",
                        "Chiến Binh","Ảnh Phong","Kim Cang","Sinh Lực","Thần Tài","Gói hỗ trợ\n30 xu","Quay lại");
                case 4 -> createOtherMenu(p,DAILY,event().status(p),"Nhận mốc\n500 quái","Nhận mốc\n3 boss","Nhận mốc\ntặng 5 thiệp","Quay lại");
                case 5 -> createOtherMenu(p,TOP,event().leaderboard(p),"Nhận quà\n500 điểm","Nhận quà\nTop tháng","Quay lại");
                case 6 -> createOtherMenu(p,MAIN,NovemberEventService.guide(),"Ghép\nThiệp","Tặng\nThiệp","Hộp quà","Đổi\nCải trang",
                        "Nhiệm vụ\nngày","Đua Top","Hướng dẫn","Đóng");
                default -> { }
            }
        } else if (menu==CRAFT || menu==DONATE) {
            if (select>3) { openBaseMenu(p); return; }
            int count=select==0?1:select==1?10:select==2?100:menu==CRAFT
                    ?Math.min(1000,Math.min(NovemberEventService.count(p,2400)/10,NovemberEventService.count(p,2401)/5))
                    :Math.min(1000,NovemberEventService.count(p,2403));
            result(p,menu==CRAFT?event().craftCards(p,count):event().donate(p,count));
        } else if (menu==BOX) {
            if(select==0 || select==1)result(p,event().craftBoxes(p,select==0?1:10));
            else if(select==2 || select==3)result(p,event().openBox(p,select==3)); else openBaseMenu(p);
        } else if (menu==SHOP) {
            if(select<5) {
                String stats=switch(select) {
                    case 0->"SĐ +32%, HP/KI +25%, chí mạng +8%.";
                    case 1->"SĐ +26%, HP/KI +27%, né đòn +18%, tốc độ +10%.";
                    case 2->"SĐ +24%, HP +30%, KI +24%, giáp +32%, phản đòn 10%.";
                    case 3->"SĐ +25%, HP/KI +32%, hồi HP/KI 3% mỗi 30 giây.";
                    default->"Giảm 50% HP/KI/SĐ; +150% vàng từ quái; +20% SM/TN; hút 5% sát thương đánh quái thành HP.";
                };
                createOtherMenu(p,MODEL+select,NovemberRules.NAMES[select]+"\n"+stats,
                        "3 ngày\n50 xu","7 ngày\n150 xu","30 ngày\n500 xu","Vĩnh viễn\n1.500 xu","Quay lại");
            } else if(select==5) result(p,event().buySupport(p)); else openBaseMenu(p);
        } else if (menu>=MODEL && menu<MODEL+5) {
            if(select<4) result(p,event().buyCostume(p,menu-MODEL,select)); else openBaseMenu(p);
        } else if (menu==DAILY) {
            if(select<3)result(p,event().claimDaily(p,select));else openBaseMenu(p);
        } else if (menu==TOP) {
            if(select==0)result(p,event().claimParticipation(p));
            else if(select==1)createOtherMenu(p,TOP_PICK,"Chọn mẫu cải trang nhận thưởng Top tháng sau 30/11.",
                    "Chiến Binh","Ảnh Phong","Kim Cang","Sinh Lực","Thần Tài","Quay lại");
            else openBaseMenu(p);
        } else if (menu==TOP_PICK) {
            if(select<5)result(p,event().claimTop(p,select));else openBaseMenu(p);
        }
    }
}
