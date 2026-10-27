package nro.services.func.DoiQua;

import nro.jdbc.daos.PlayerDAO;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.services.InventoryService;
import nro.services.ItemService;
import nro.services.PetService;
import nro.services.Service;

public class DoiVND {

    public static final int PAY_BALANCE = 0;
    public static final int PAY_RUBY = 1;

    /**
     * Legacy: mua vật phẩm bằng số dư tài khoản.
     */
    public static void MuaDeTu(Player player, int vND, int iD) {
        if (player.soDuVND < vND) {
            Service.gI().sendThongBao(player, "Bạn không đủ số dư để mua vật phẩm này");
            return;
        }

        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang");
            return;
        }

        Item phanQua = ItemService.gI().createNewItem((short) iD);
        InventoryService.gI().addItemBag(player, phanQua, 999);

        // PlayerDAO.subVndBar đã tự trừ player.soDuVND, không trừ lần 2 ở đây.
        PlayerDAO.subVndBar(player, vND);

        InventoryService.gI().sendItemBags(player);
        Service.getInstance().sendMoney(player);
        Service.gI().sendThongBao(player,
                "Bạn nhận được " + phanQua.template.name
                        + "\nSố dư còn lại: " + nro.utils.Util.numberToMoney(player.soDuVND) + " VNĐ");
    }

    /**
     * Mua đệ tử VIP trực tiếp, không tạo hộp trong hành trang.
     *
     * typePet:
     * 2 = Kid Bill
     * 3 = Kid Fide
     * 4 = Black Goku Rose (dùng cờ isSuperPicolo để tương thích dữ liệu cũ)
     */
    public static void MuaDeTuVip(Player player, int price, int typePet, int paymentMethod) {
        if (player == null) {
            return;
        }
        if (player.pet == null) {
            Service.gI().sendThongBao(player, "Bạn cần có đệ tử trước khi mua đệ tử VIP");
            return;
        }
        if (typePet < 2 || typePet > 4) {
            Service.gI().sendThongBao(player, "Loại đệ tử VIP không hợp lệ");
            return;
        }
        if (isSameVipPet(player, typePet)) {
            Service.gI().sendThongBao(player, "Bạn đang sở hữu đệ tử VIP này rồi");
            return;
        }

        synchronized (player) {
            if (paymentMethod == PAY_BALANCE) {
                if (player.soDuVND < price) {
                    Service.gI().sendThongBao(player,
                            "Không đủ số dư. Cần " + price + " VNĐ");
                    return;
                }
                PlayerDAO.subVndBar(player, price);
            } else if (paymentMethod == PAY_RUBY) {
                if (!player.inventory.trySpendRuby(price)) {
                    Service.gI().sendThongBao(player,
                            "Không đủ Hồng Ngọc. Cần " + price + " Hồng Ngọc");
                    return;
                }
            } else {
                Service.gI().sendThongBao(player, "Phương thức thanh toán không hợp lệ");
                return;
            }

            int gender = player.pet.gender;
            PetService.gI().changeSuperPet(player, gender, typePet);

            Service.getInstance().sendMoney(player);
            Service.gI().sendThongBao(player,
                    "Mua " + getVipPetName(typePet) + " thành công bằng "
                            + (paymentMethod == PAY_BALANCE ? "số dư tài khoản" : "Hồng Ngọc"));
        }
    }

    private static boolean isSameVipPet(Player player, int typePet) {
        if (player.pet == null) {
            return false;
        }
        return switch (typePet) {
            case 2 -> player.pet.isBillNhi;
            case 3 -> player.pet.isFideTrau;
            case 4 -> player.pet.isSuperPicolo;
            default -> false;
        };
    }

    private static String getVipPetName(int typePet) {
        return switch (typePet) {
            case 2 -> "Đệ VIP Kid Bill";
            case 3 -> "Đệ VIP Kid Fide";
            case 4 -> "Đệ VIP Black Goku Rose";
            default -> "Đệ VIP";
        };
    }
}
