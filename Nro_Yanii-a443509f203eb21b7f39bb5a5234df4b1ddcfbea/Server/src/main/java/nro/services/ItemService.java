package nro.services;

import nro.models.item.ItemOptionTemplate;
import nro.models.item.ItemTemplate;
import nro.models.kigui.KiGuiItem;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.shop.ItemShop;
import nro.server.Manager;
import nro.utils.TimeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import nro.models.player.Player;
import nro.utils.Util;

/**
 * @author 💖 Nro Yanii 💖
 *
 */
public class ItemService {

    private static ItemService i;

    public static ItemService gI() {
        if (i == null) {
            i = new ItemService();
        }
        return i;
    }

    public Item createItemNull() {
        Item item = new Item();
        return item;
    }

    public Item createItemFromItemShop(ItemShop itemShop) {
        Item item = new Item();
        item.template = itemShop.temp;
        item.quantity = 1;
        item.content = item.getContent();
        item.info = item.getInfo();
        for (ItemOption io : itemShop.options) {
            item.itemOptions.add(new ItemOption(io));

            removeAndAddOptionTemplate(item.itemOptions, new ItemOption(io).optionTemplate.id);
        }
        return item;
    }

    public Item randomCS_DHD(int itemId, int gender) {
        Item it = createItemSetKichHoat(itemId, 1);
        List<Integer> ao = Arrays.asList(650, 652, 654);
        List<Integer> quan = Arrays.asList(651, 653, 655);
        List<Integer> gang = Arrays.asList(657, 659, 661);
        List<Integer> giay = Arrays.asList(658, 660, 662);
        int nhd = 656;
        if (ao.contains(itemId)) {
            it.itemOptions.add(new ItemOption(47, Util.highlightsItem(gender == 2, new Random().nextInt(1001) + 1800))); // áo
            // từ
            // 1800-2800
            // giáp
        }
        if (quan.contains(itemId)) {
            it.itemOptions.add(new ItemOption(22, Util.highlightsItem(gender == 0, new Random().nextInt(16) + 85))); // hp
            // 85-100k
        }
        if (gang.contains(itemId)) {
            it.itemOptions.add(new ItemOption(0, Util.highlightsItem(gender == 2, new Random().nextInt(150) + 8500))); // 8500-10000
        }
        if (giay.contains(itemId)) {
            it.itemOptions.add(new ItemOption(23, Util.highlightsItem(gender == 1, new Random().nextInt(11) + 80))); // ki
            // 80-90k
        }
        if (nhd == itemId) {
            it.itemOptions.add(new ItemOption(14, new Random().nextInt(3) + 17)); // chí mạng 17-19%
        }
        it.itemOptions.add(new ItemOption(21, 80));// yêu cầu sm 80 tỉ
        it.itemOptions.add(new ItemOption(30, 1));// ko the gd
        return it;
    }

    public void OpenDHD2(Player player, int gender, int itemtype) {

        int[][] items = {
            {650, 651, 657, 658, 656},
            {652, 653, 659, 660, 656},
            {654, 655, 661, 662, 656}
        }; // td, namec,xd
        Item item = randomCS_DHD(items[gender][itemtype], gender);

        if (item != null && InventoryService.gI().getCountEmptyBag(player) > 0) {
            InventoryService.gI().addItemBag(player, item, 1);
            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
        }
    }

    public Item copyItem(Item item) {
        Item it = new Item();
        it.itemOptions = new ArrayList<>();
        it.template = item.template;
        it.info = item.info;
        it.content = item.content;
        it.quantity = item.quantity;
        it.createTime = item.createTime;
        for (ItemOption io : item.itemOptions) {
            it.itemOptions.add(new ItemOption(io));
        }
        return it;
    }

    public KiGuiItem convertToConsignmentItem(Item item) {
        KiGuiItem it = new KiGuiItem();
        it.itemOptions = new ArrayList<>();
        it.template = item.template;
        it.info = item.info;
        it.content = item.content;
        it.quantity = item.quantity;
        it.createTime = item.createTime;
        for (ItemOption io : item.itemOptions) {
            it.itemOptions.add(new ItemOption(io));
        }
        it.setPriceGold(-1);
        it.setPriceGem(-1);
        return it;
    }

    public Item createNewItem(short tempId) {
        return createNewItem(tempId, 1);
    }

    public Item createNewItem(short tempId, int quantity) {
        Item item = new Item();
        item.template = getTemplate(tempId);
        item.quantity = quantity;
        item.createTime = System.currentTimeMillis();

        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }

    public KiGuiItem createNewConsignmentItem(short tempId, int quantity) {
        KiGuiItem item = new KiGuiItem();
        item.template = getTemplate(tempId);
        item.quantity = quantity;
        item.createTime = System.currentTimeMillis();
        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }

    public Item createItemFromItemMap(ItemMap itemMap) {
        Item item = createNewItem(itemMap.itemTemplate.id, itemMap.quantity);
        item.itemOptions = itemMap.options;
        return item;
    }

    public ItemOptionTemplate getItemOptionTemplate(int id) {
        return Manager.ITEM_OPTION_TEMPLATES.get(id);
    }

    public ItemTemplate getTemplate(int id) {
        return Manager.ITEM_TEMPLATES.get(id);
    }

    public boolean isItemActivation(Item item) {
        return false;
    }

    public Item createItemSetKichHoat(int tempId, int quantity) {
        Item item = new Item();
        item.template = getTemplate(tempId);
        item.quantity = quantity;
        item.itemOptions = createItemNull().itemOptions;
        item.createTime = System.currentTimeMillis();
        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }

    public Item DoThienSu(int itemId, int gender) {
        Item dots = createItemSetKichHoat(itemId, 1);
        List<Integer> ao = Arrays.asList(1048, 1049, 1050);
        List<Integer> quan = Arrays.asList(1051, 1052, 1053);
        List<Integer> gang = Arrays.asList(1054, 1055, 1056);
        List<Integer> giay = Arrays.asList(1057, 1058, 1059);
        List<Integer> nhan = Arrays.asList(1060, 1061, 1062);
        // áo
        if (ao.contains(itemId)) {
            dots.itemOptions
                    .add(new ItemOption(47, Util.highlightsItem(gender == 2, new Random().nextInt(1201) + 2800))); // áo
            // từ
            // 2800-4000
            // giáp
            if (Util.isTrue(30, 100)) {
                dots.itemOptions.add(new ItemOption(108, Util.nextInt(3, 10)));
            }
        }
        // quần
        if (Util.isTrue(60, 100)) {
            if (quan.contains(itemId)) {
                dots.itemOptions
                        .add(new ItemOption(22, Util.highlightsItem(gender == 0, new Random().nextInt(11) + 120))); // hp
                // 120k-130k
            }
        } else {
            if (quan.contains(itemId)) {
                dots.itemOptions
                        .add(new ItemOption(22, Util.highlightsItem(gender == 0, new Random().nextInt(51) + 130))); // hp
                // 130-180k
                // 15%
                if (Util.isTrue(30, 100)) {
                    dots.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
                }
            }
        }
        // găng
        if (Util.isTrue(60, 100)) {
            if (gang.contains(itemId)) {
                dots.itemOptions
                        .add(new ItemOption(0, Util.highlightsItem(gender == 2, new Random().nextInt(7651) + 11000))); // 11000-18600
            }
        } else {
            if (gang.contains(itemId)) {
                dots.itemOptions
                        .add(new ItemOption(0, Util.highlightsItem(gender == 2, new Random().nextInt(7001) + 12000))); // gang
                // 15%
                // 12-19k
                // -xayda
                // 12k1
                if (Util.isTrue(30, 100)) {
                    dots.itemOptions.add(new ItemOption(50, Util.nextInt(3, 10)));
                }
            }
        }
        // giày
        if (Util.isTrue(60, 100)) {
            if (giay.contains(itemId)) {
                dots.itemOptions
                        .add(new ItemOption(23, Util.highlightsItem(gender == 1, new Random().nextInt(21) + 120))); // ki
                // 90-110k
            }
        } else {
            if (giay.contains(itemId)) {
                dots.itemOptions
                        .add(new ItemOption(23, Util.highlightsItem(gender == 1, new Random().nextInt(21) + 130))); // ki
                // 110-130k
                if (Util.isTrue(30, 100)) {
                    dots.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
                }
            }
        }

        if (nhan.contains(itemId)) {
            dots.itemOptions.add(new ItemOption(14, Util.highlightsItem(gender == 1, new Random().nextInt(3) + 18))); // nhẫn
            // 18-20%

        }
        dots.itemOptions.add(new ItemOption(21, 100));
        dots.itemOptions.add(new ItemOption(30, 1));
        return dots;
    }

    public int getPercentTrainArmor(Item item) {
        if (item != null) {
            switch (item.template.id) {
                case 529:
                case 534:
                    return 10;
                case 530:
                case 535:
                    return 20;
                case 531:
                case 536:
                    return 30;
                default:
                    return 0;
            }
        } else {
            return 0;
        }
    }

    public boolean isTrainArmor(Item item) {
        if (item != null) {
            switch (item.template.id) {
                case 529:
                case 534:
                case 530:
                case 535:
                case 531:
                case 536:
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public boolean isOutOfDateTime(Item item) {
        long now = System.currentTimeMillis();
        if (item != null) {
            for (ItemOption io : item.itemOptions) {
                if (io.optionTemplate.id == 93) {
                    int dayPass = (int) TimeUtil.diffDate(new Date(), new Date(item.createTime), TimeUtil.DAY);
                    if (dayPass != 0) {
                        io.param -= dayPass;
                        if (io.param <= 0) {
                            return true;
                        } else {
                            item.createTime = System.currentTimeMillis();
                        }
                    }
                } else if (io.optionTemplate.id == 196) {
                    long e = io.param * 1000L;
                    if (e <= now) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isItemNoLimitQuantity(int id) {// item k giới hạn số lượng
        if (id >= 1066 && id <= 1070) {// mảnh trang bị thiên sứ
            return true;
        }
        return false;
    }

    public void removeAndAddOptionTemplate(List<ItemOption> itemOptions, int removeId) {
        int id = 0;
        int param = 0;
        boolean shouldExecute = false;

        // Tạo một tỷ lệ ngẫu nhiên (0-100)
        if (Util.isTrue(1, 100)) {
            shouldExecute = false;  // 50% cơ hội không thực hiện (không ra option 93)
        } else {
            switch (removeId) {
                case 228:
                    id = 93;
                    param = Util.nextInt(1, 3);
                    shouldExecute = true;  // Nếu là removeId 228, thay bằng option 93
                    break;
                default:
                    // Nếu không khớp với các trường hợp trên, không thực hiện thay đổi
                    break;
            }
        }

        // Nếu không thêm option 93 (tỷ lệ ngẫu nhiên), xóa option 228 nếu có
        if (!shouldExecute) {
            itemOptions.removeIf(io -> io.optionTemplate.id == 228); // Xóa optionTemplate có id = 228 nếu có
        }

        // Nếu thực hiện (thêm option 93), xóa option removeId (nếu có) và thêm option 93
        if (shouldExecute && itemOptions.stream().anyMatch(io -> io.optionTemplate.id == removeId)) {
            itemOptions.removeIf(io -> io.optionTemplate.id == removeId); // Xóa optionTemplate có id cần xóa
            itemOptions.add(new ItemOption(new ItemOption(id, param))); // Thêm optionTemplate mới với id = 93
        }
    }
}
