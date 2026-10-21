package nro.models.skill;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author outcast c-cute hột me 😳
 */
@Setter
@Getter
public class Hit {
    private List<Integer> hits;
    private Map<Integer, Integer> targets;

    public Hit() {
        hits = new ArrayList<>();
        targets = new HashMap<>();
    }

    public void addTarget(int targetID, int type) {
        targets.put(targetID, type);
    }

    public void addHit(int damage) {
        hits.add(damage);
    }
}
