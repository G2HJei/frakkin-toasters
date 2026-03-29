package xyz.zlatanov.frakkintoasters.ship;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.exception.FrakCallTheAdmiralException;

@Getter
@Accessors(fluent = true)
public class ShipsHolder {
    private int basestars;
    private int raiders;
    private int heavyRaiders;
    private int centurions;

    private final int basestarsLimit;
    private final int raidersLimit;
    private final int heavyRaidersLimit;
    private final int centurionsLimit;

    @Builder
    private ShipsHolder(int basestars, int raiders, int heavyRaiders, int centurions) {
        this.basestars = basestars;
        this.raiders = raiders;
        this.heavyRaiders = heavyRaiders;
        this.centurions = centurions;
        basestarsLimit = basestars;
        raidersLimit = raiders;
        heavyRaidersLimit = heavyRaiders;
        centurionsLimit = centurions;
    }

    public static class ShipsHolderBuilder {
        private int basestars    = 2;
        private int raiders      = 20;
        private int heavyRaiders = 4;
        private int centurions   = 4;
    }

    public Basestar basestar() {
        if (basestars == 0) {
            throw new FrakCallTheAdmiralException();
        }
        basestars--;
        return new Basestar();
    }

    public Raider raider() {
        if (raiders == 0) {
            throw new FrakCallTheAdmiralException();
        }
        raiders--;
        return new Raider();
    }

    public HeavyRaider heavyRaider() {
        if (heavyRaiders == 0) {
            throw new FrakCallTheAdmiralException();
        }
        heavyRaiders--;
        return new HeavyRaider();
    }

    public Centurion centurion() {
        if (centurions == 0) {
            throw new FrakCallTheAdmiralException();
        }
        centurions--;
        return new Centurion();
    }

    public void removed(ShipType shipType) {
        switch (shipType) {
            case BASESTAR -> {
                if (basestars >= basestarsLimit) {
                    throw new FrakCallTheAdmiralException();
                }
                basestars++;
            }
            case HEAVY_RAIDER -> {
                if (heavyRaiders >= heavyRaidersLimit) {
                    throw new FrakCallTheAdmiralException();
                }
                heavyRaiders++;
            }
            case RAIDER -> {
                if (raiders >= raidersLimit) {
                    throw new FrakCallTheAdmiralException();
                }
                raiders++;
            }
            default -> throw new FrakCallTheAdmiralException();
        }
    }

    public void removedCenturion() {
        if (centurions >= centurionsLimit) {
            throw new FrakCallTheAdmiralException();
        }
        centurions++;
    }
}
