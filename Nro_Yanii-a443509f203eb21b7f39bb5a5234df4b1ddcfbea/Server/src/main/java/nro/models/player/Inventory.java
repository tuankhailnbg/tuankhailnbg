package nro.models.player;

import java.util.ArrayList;
import java.util.List;
import nro.models.item.Item;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class Inventory {

    public static final long LIMIT_GOLD = 500_000_000_000L;

    private Player player;

    public Item trainArmor;

    public List<Item> itemsBody;
    public List<Item> itemsBag;
    public List<Item> itemsBox;

    public List<Item> itemsBoxCrackBall;
    // public List<Item> itemsReward;

    public long gold, goldLimit;
    public int gem;
    public int ruby;

    // public int goldBar;
    public Inventory(Player player) {
        this.player = player;
        itemsBody = new ArrayList<>();
        itemsBag = new ArrayList<>();
        itemsBox = new ArrayList<>();
        itemsBoxCrackBall = new ArrayList<>();
        // itemsReward = new ArrayList<>();
    }

    public int getGem() {
        return this.gem;
    }

    public long getGold() {
        return this.gold;
    }

    public long getGoldLimit() {
        return goldLimit + LIMIT_GOLD;
    }

    public long getGoldDisplay() {
        long amount = gold;
        if (amount > Integer.MAX_VALUE && !player.isVersionAbove(214)) {
            return Integer.MAX_VALUE;
        }
        return amount;
    }

    public long getRuby() {
        return this.ruby;
    }

    public boolean hasGold(long amount) {
        if (amount < 0) {
            return false;
        }
        return this.gold >= amount;
    }

    public synchronized boolean trySpendGold(long amount) {
        if (amount <= 0) {
            return false;
        }
        if (this.gold < amount) {
            return false;
        }
        this.gold -= amount;
        return true;
    }

    public void subGold(int num) {
        subGold((long) num);
    }

    public synchronized void subGold(long num) {
        if (num <= 0) {
            return;
        }
        if (this.gold >= num) {
            this.gold -= num;
        } else {
            nro.utils.Log.error("[INVENTORY_SUBGOLD_GUARD] Attempted to subGold " + num + " with only " + this.gold + " gold! Player: " + (player != null ? player.name : "null"));
        }
    }

    public boolean hasGem(int amount) {
        if (amount < 0) {
            return false;
        }
        return this.gem >= amount;
    }

    public synchronized boolean trySpendGem(int amount) {
        if (amount <= 0) {
            return false;
        }
        if (this.gem < amount) {
            return false;
        }
        this.gem -= amount;
        return true;
    }

    public synchronized void subGem(int num) {
        if (num <= 0) {
            return;
        }
        if (this.gem >= num) {
            this.gem -= num;
        } else {
            nro.utils.Log.error("[INVENTORY_SUBGEM_GUARD] Attempted to subGem " + num + " with only " + this.gem + " gem! Player: " + (player != null ? player.name : "null"));
        }
    }

    public boolean hasRuby(int amount) {
        if (amount < 0) {
            return false;
        }
        return this.ruby >= amount;
    }

    public synchronized boolean trySpendRuby(int amount) {
        if (amount <= 0) {
            return false;
        }
        if (this.ruby < amount) {
            return false;
        }
        this.ruby -= amount;
        return true;
    }

    public synchronized void subRuby(int num) {
        if (num <= 0) {
            return;
        }
        if (this.ruby >= num) {
            this.ruby -= num;
        } else {
            nro.utils.Log.error("[INVENTORY_SUBRUBY_GUARD] Attempted to subRuby " + num + " with only " + this.ruby + " ruby! Player: " + (player != null ? player.name : "null"));
        }
    }

    public void addGold(long gold) {
        if (gold <= 0) {
            return;
        }
        this.gold += gold;
        long goldLimit = getGoldLimit();
        if (this.gold > goldLimit) {
            this.gold = goldLimit;
        }
    }

    public void addGem(int gem) {
        if (gem <= 0) {
            return;
        }
        this.gem += gem;
    }

    public void addRuby(int ruby) {
        if (ruby <= 0) {
            return;
        }
        this.ruby += ruby;
    }

    public void sanitizeCurrencies() {
        if (this.gold < 0) {
            nro.utils.Log.warning(String.format("[GOLD_SANITIZE] Player=%s OldGold=%d NewGold=0", (player != null ? player.name : "unknown"), this.gold));
            this.gold = 0;
        }
        if (this.gem < 0) {
            this.gem = 0;
        }
        if (this.ruby < 0) {
            this.ruby = 0;
        }
    }

    public void dispose() {
        this.player = null;
        if (this.trainArmor != null) {
            this.trainArmor.dispose();
        }
        this.trainArmor = null;
        if (this.itemsBody != null) {
            for (Item it : this.itemsBody) {
                it.dispose();
            }
            this.itemsBody.clear();
        }
        if (this.itemsBag != null) {
            for (Item it : this.itemsBag) {
                it.dispose();
            }
            this.itemsBag.clear();
        }
        if (this.itemsBox != null) {
            for (Item it : this.itemsBox) {
                it.dispose();
            }
            this.itemsBox.clear();
        }
        if (this.itemsBoxCrackBall != null) {
            for (Item it : this.itemsBoxCrackBall) {
                it.dispose();
            }
            this.itemsBoxCrackBall.clear();
        }
        this.itemsBody = null;
        this.itemsBag = null;
        this.itemsBox = null;
        this.itemsBoxCrackBall = null;
    }
}
