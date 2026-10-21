package nro.models.item;

import nro.services.ItemService;
import nro.utils.Util;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Kitak
 */
public class ItemOption {

    private static Map<String, String> OPTION_STRING = new HashMap<String, String>();

    public int param;

    public ItemOptionTemplate optionTemplate;
    public byte activeCard;

    public ItemOption() {
    }

    public ItemOption(ItemOption io) {
        this.param = io.param;
        this.optionTemplate = io.optionTemplate;
    }

    public ItemOption(int tempId, int param) {
        this.optionTemplate = ItemService.gI().getItemOptionTemplate(tempId);
        this.param = param;
    }

    public String getOptionString() {
        String key = this.optionTemplate.name + "#" + this.param + "#";
        String value = OPTION_STRING.get(key);
        if (value == null) {
            value = Util.replace(this.optionTemplate.name, "#", String.valueOf(this.param));
            OPTION_STRING.put(key, value);
        }
        return value;
    }
    
    public String getOptionString(int param) {
        String key = this.optionTemplate.name + "#" + param + "#";
        String value = OPTION_STRING.get(key);
        if (value == null) {
            value = Util.replace(this.optionTemplate.name, "#", String.valueOf(param));
            OPTION_STRING.put(key, value);
        }
        return value;
    }

    public ItemOption format() {
        int id = optionTemplate.id;
        int param = this.param;
        if (param > Short.MAX_VALUE) {
            boolean changed = false;
            switch (id) {
                case 6:
                    id = 22;
                    param /= 1000;
                    changed = true;
                    break;

                case 7:
                    id = 23;
                    param /= 1000;
                    changed = true;
                    break;

                case 31:
                    id = 171;
                    param /= 1000;
                    changed = true;
                    break;

                case 48:
                    id = 2;
                    param /= 1000;
                    changed = true;
                    break;
            }
            if (changed) {
                return new ItemOption(id, param);
            }
        }
        return this;
    }

    public void dispose() {
        this.optionTemplate = null;
    }
}
