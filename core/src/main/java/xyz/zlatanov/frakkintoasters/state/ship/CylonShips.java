package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Accessors(fluent = true)
public class CylonShips {

    private static final int ID_OFFSET = 1000;

    private final List<Basestar>    basestars;
    private final List<Raider>      raiders;
    private final List<HeavyRaider> heavyRaiders;
    private final List<Centurion>   centurions;

    private CylonShips(int basestarCount, int raiderCount, int heavyRaiderCount, int centurionCount) {
        int id = ID_OFFSET;
        basestars = new ArrayList<>();
        for (int i = 0; i < basestarCount; i++) {
            basestars.add(new Basestar(id++));
        }
        raiders = new ArrayList<>();
        for (int i = 0; i < raiderCount; i++) {
            raiders.add(new Raider(id++));
        }
        heavyRaiders = new ArrayList<>();
        for (int i = 0; i < heavyRaiderCount; i++) {
            heavyRaiders.add(new HeavyRaider(id++));
        }
        centurions = new ArrayList<>();
        for (int i = 0; i < centurionCount; i++) {
            centurions.add(new Centurion(id++));
        }
    }

    public static CylonShipsBuilder builder() {
        return new CylonShipsBuilder();
    }

    public Optional<Basestar> basestar() {
        if (basestars.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(basestars.removeLast());
    }

    public Optional<Raider> raider() {
        if (raiders.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(raiders.removeLast());
    }

    public Optional<HeavyRaider> heavyRaider() {
        if (heavyRaiders.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(heavyRaiders.removeLast());
    }

    public Optional<Centurion> centurion() {
        if (centurions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(centurions.removeLast());
    }

    public CylonShips returned(Ship ship) {
        switch (ship) {
            case Basestar b -> {
                b.clearDamage();
                basestars.add(b);
            }
            case Raider r -> raiders.add(r);
            case HeavyRaider h -> heavyRaiders.add(h);
            default -> throw new FrakCallTheAdmiralException();
        }
        return this;
    }

    public CylonShips returnedCenturion(Centurion centurion) {
        centurions.add(centurion);
        return this;
    }

    public static class CylonShipsBuilder {
        private int basestars    = 2;
        private int raiders      = 20;
        private int heavyRaiders = 4;
        private int centurions   = 4;

        public CylonShipsBuilder basestars(int basestars) {
            this.basestars = basestars;
            return this;
        }

        public CylonShipsBuilder raiders(int raiders) {
            this.raiders = raiders;
            return this;
        }

        public CylonShipsBuilder heavyRaiders(int heavyRaiders) {
            this.heavyRaiders = heavyRaiders;
            return this;
        }

        public CylonShipsBuilder centurions(int centurions) {
            this.centurions = centurions;
            return this;
        }

        public CylonShips build() {
            return new CylonShips(basestars, raiders, heavyRaiders, centurions);
        }
    }
}
