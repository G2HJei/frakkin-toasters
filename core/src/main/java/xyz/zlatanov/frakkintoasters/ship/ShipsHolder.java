package xyz.zlatanov.frakkintoasters.ship;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.exception.FrakCallTheAdmiralException;

@Builder
@Getter
@Accessors(fluent = true)
public class ShipsHolder {
    @Builder.Default
    private int basestars    = 2;
    @Builder.Default
    private int raiders      = 20;
    @Builder.Default
    private int heavyRaiders = 4;
    @Builder.Default
    private int centurions   = 4;

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
            case BASESTAR -> basestars++;
            case HEAVY_RAIDER -> heavyRaiders++;
            case RAIDER -> raiders++;
            default -> throw new FrakCallTheAdmiralException();
        }
        assert basestars < 3 && raiders < 21 && heavyRaiders < 4;
    }

    public void removedCenturion() {
        centurions++;
        assert centurions < 4;
    }
}
