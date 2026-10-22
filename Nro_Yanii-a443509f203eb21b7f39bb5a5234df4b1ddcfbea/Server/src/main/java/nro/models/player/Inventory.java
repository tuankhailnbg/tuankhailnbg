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
    public static final long MAX_GOLD_LIMIT = 1_000_000_000_000L;

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
        if (this.goldLimit < 0) {
            this.goldLimit = 0;
        }
        long total = goldLimit + LIMIT_GOLD;
        if (total > MAX_GOLD_LIMIT) {
            return MAX_GOLD_LIMIT;
        }
        return total;
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

    public synchronized boolean trySpendCurrencies(long gold, int gem, int ruby) {
        if (gold < 0 || gem < 0 || ruby < 0) {
            return false;
        }
        if (gold > 0 && this.gold < gold) {
            return false;
        }
        if (gem > 0 && this.gem < gem) {
            return false;
        }
        if (ruby > 0 && this.ruby < ruby) {
            return false;
        }
        if (gold > 0) {
            this.gold -= gold;
        }
        if (gem > 0) {
            this.gem -= gem;
        }
        if (ruby > 0) {
            this.ruby -= ruby;
        }
        return true;
    }

    public synchronized boolean trySpendGoldAndGem(long gold, int gem) {
        return trySpendCurrencies(gold, gem, 0);
    }

    public synchronized boolean trySpendGoldAndRuby(long gold, int ruby) {
        return trySpendCurrencies(gold, 0, ruby);
    }

    public synchronized boolean trySpendGemAndRuby(int gem, int ruby) {
        return trySpendCurrencies(0, gem, ruby);
    }

    public synchronized void addGold(long gold) {
        if (gold <= 0) {
            return;
        }
        if (this.gold < 0) {
            this.gold = 0;
        }
        long goldLimit = getGoldLimit();
        if (this.gold >= goldLimit) {
            this.gold = goldLimit;
            return;
        }
        if (gold >= goldLimit - this.gold) {
            this.gold = goldLimit;
        } else {
            this.gold += gold;
        }
    }

    public synchronized void addGem(int gem) {
        if (gem <= 0) {
            return;
        }
        if (this.gem < 0) {
            this.gem = 0;
        }
        long result = (long) this.gem + gem;
        if (result > Integer.MAX_VALUE) {
            this.gem = Integer.MAX_VALUE;
        } else {
            this.gem = (int) result;
        }
    }

    public synchronized void addRuby(int ruby) {
        if (ruby <= 0) {
            return;
        }
        if (this.ruby < 0) {
            this.ruby = 0;
        }
        long result = (long) this.ruby + ruby;
        if (result > Integer.MAX_VALUE) {
            this.ruby = Integer.MAX_VALUE;
        } else {
            this.ruby = (int) result;
        }
    }

    public synchronized void sanitizeCurrencies() {
        if (this.gold < 0) {
            nro.utils.Log.error(String.format("[GOLD_SANITIZE] PlayerId=%d PlayerName=%s OldGold=%d NewGold=0",
                    (player != null ? player.id : -1), (player != null ? player.name : "unknown"), this.gold));
            this.gold = 0;
        }
        if (this.gem < 0) {
            nro.utils.Log.error(String.format("[GEM_SANITIZE] PlayerId=%d PlayerName=%s OldGem=%d NewGem=0",
                    (player != null ? player.id : -1), (player != null ? player.name : "unknown"), this.gem));
            this.gem = 0;
        }
        if (this.ruby < 0) {
            nro.utils.Log.error(String.format("[RUBY_SANITIZE] PlayerId=%d PlayerName=%s OldRuby=%d NewRuby=0",
                    (player != null ? player.id : -1), (player != null ? player.name : "unknown"), this.ruby));
            this.ruby = 0;
        }
        if (this.goldLimit < 0) {
            this.goldLimit = 0;
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
