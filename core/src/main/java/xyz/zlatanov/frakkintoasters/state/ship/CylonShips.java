package xyz.zlatanov.frakkintoasters.state.ship;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

@Builder
@Getter
@Accessors(fluent = true)
public class CylonShips {
    @Builder.Default
    private int basestars    = 2;
    @Builder.Default
    private int raiders      = 20;
    @Builder.Default
    private int heavyRaiders = 4;
    @Builder.Default
    private int centurions   = 4;

    public Basestar basestar() {
        assert basestars > 0;
        basestars--;
        return new Basestar();
    }

    public Raider raider() {
        assert raiders > 0;
        raiders--;
        return new Raider();
    }

    public HeavyRaider heavyRaider() {
        assert heavyRaiders > 0;
        heavyRaiders--;
        return new HeavyRaider();
    }

    public Centurion centurion() {
        assert centurions > 0;
        centurions--;
        return new Centurion();
    }

    public void removed(ShipType shipType) {
        switch (shipType) {
            case BASESTAR -> {
                assert basestars < 2;
                basestars++;
            }
            case HEAVY_RAIDER -> {
                assert heavyRaiders < 4;
                heavyRaiders++;
            }
            case RAIDER -> {
                assert raiders < 20;
                raiders++;
            }
            default -> throw new FrakCallTheAdmiralException();
        }
    }

    public void removedCenturion() {
        assert centurions < 4;
        centurions++;
    }
}
