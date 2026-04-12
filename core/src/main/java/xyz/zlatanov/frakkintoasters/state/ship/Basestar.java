package xyz.zlatanov.frakkintoasters.state.ship;

import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.BASESTAR;

public class Basestar implements Ship {

    private final List<BasestarDamage> damage = new ArrayList<>();

    @Override
    public ShipType type() {
        return BASESTAR;
    }

    public Basestar damage(BasestarDamage dmg) {
        damage.add(dmg);
        return this;
    }

    public List<BasestarDamage> damage() {
        return Collections.unmodifiableList(damage);
    }
}
