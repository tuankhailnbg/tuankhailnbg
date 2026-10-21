/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.attr;

import nro.utils.TimeUtil;
import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
@Getter
public class Attribute {

    private int id;
    private int value;
    private int time;
    private AttributeTemplate template;
    private transient boolean changed;

    @Builder
    public Attribute(int id, int templateID, int value, int time) {
        this.id = id;
        this.value = value;
        this.time = time;
        this.template = AttributeTemplateManager.getInstance().find(templateID);
    }

    public boolean update() {
        if (time > 0) {
            time--;
            setChanged();
            return true;
        }
        return false;
    }

    public void setChanged() {
        changed = true;
    }

    public boolean isExpired() {
        return time == 0;
    }

    @Override
    public String toString() {
        String text = template.getName().replaceAll("#value", String.valueOf(this.value));
        if (time != -1) {
            String strTimeAgo = TimeUtil.getTimeAgo(time);
            text += " trong vòng " + strTimeAgo;
        }
        return text;

    }

    public void setValue(int value) {
        this.value = value;
        setChanged();
    }

    public void setTime(int time) {
        this.time = time;
        setChanged();
    }

    public void setTemplate(AttributeTemplate template) {
        this.template = template;
        setChanged();
    }

}
