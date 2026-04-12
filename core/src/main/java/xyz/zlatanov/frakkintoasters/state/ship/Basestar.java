package xyz.zlatanov.frakkintoasters.state.ship;

import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.BASESTAR;

public class Basestar implements Ship {

    private final int                 id;
    private final List<BasestarDamage> damage = new ArrayList<>();

    public Basestar(int id) {
        this.id = id;
    }

    @Override
    public int id() {
        return id;
    }

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

    public void clearDamage() {
        damage.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Basestar basestar)) return false;
        return id == basestar.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
