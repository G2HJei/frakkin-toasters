package xyz.zlatanov.frakkintoasters.state.ship;

import xyz.zlatanov.frakkintoasters.state.Mutates;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.view.CylonShipsView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Accessors(fluent = true)
public class CylonShips implements CylonShipsView {

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

    @Mutates
    public Optional<Basestar> basestar() {
        if (basestars.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(basestars.removeLast());
    }

    @Mutates
    public Optional<Raider> raider() {
        if (raiders.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(raiders.removeLast());
    }

    @Mutates
    public Optional<HeavyRaider> heavyRaider() {
        if (heavyRaiders.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(heavyRaiders.removeLast());
    }

    @Mutates
    public Optional<Centurion> centurion() {
        if (centurions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(centurions.removeLast());
    }

    @Override
    public int available(ShipType shipType) {
        return switch (shipType) {
            case BASESTAR -> basestars.size();
            case RAIDER -> raiders.size();
            case HEAVY_RAIDER -> heavyRaiders.size();
            default -> throw new FrakCallTheAdmiralException();
        };
    }

    /**
     * @return the ship that the matching remover (e.g. {@link #raider()}) would hand out next, without removing it
     */
    public <T extends CylonShip> Optional<T> next(Class<T> type) {
        val supply = supplyOf(type);
        return supply.isEmpty() ? Optional.empty() : Optional.of(supply.getLast());
    }

    /**
     * @return the centurion that {@link #centurion()} would hand out next, without removing it
     */
    public Optional<Centurion> nextCenturion() {
        return centurions.isEmpty() ? Optional.empty() : Optional.of(centurions.getLast());
    }

    /**
     * Removes the ship with this id from the supply.
     */
    @Mutates
    public CylonShip take(int id) {
        for (val supply : List.of(basestars, raiders, heavyRaiders)) {
            for (val ship : supply) {
                if (ship.id() == id) {
                    supply.remove(ship);
                    return ship;
                }
            }
        }
        throw new FrakCallTheAdmiralException();
    }

    /**
     * Removes the centurion with this id from the supply.
     */
    @Mutates
    public Centurion takeCenturion(int id) {
        val centurion = centurions.stream()
                .filter(c -> c.id() == id)
                .findFirst()
                .orElseThrow(FrakCallTheAdmiralException::new);
        centurions.remove(centurion);
        return centurion;
    }

    @SuppressWarnings("unchecked")  // the class token selects the list of exactly that ship type, so the cast is safe
    private <T extends CylonShip> List<T> supplyOf(Class<T> type) {
        if (type == Basestar.class) {
            return (List<T>) basestars;
        } else if (type == Raider.class) {
            return (List<T>) raiders;
        } else if (type == HeavyRaider.class) {
            return (List<T>) heavyRaiders;
        }
        throw new FrakCallTheAdmiralException();
    }

    @Mutates
    public CylonShips returned(Ship ship) {
        switch (ship) {
            case Basestar b -> basestars.add(b);
            case Raider r -> raiders.add(r);
            case HeavyRaider h -> heavyRaiders.add(h);
            default -> throw new FrakCallTheAdmiralException();
        }
        return this;
    }

    @Mutates
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
