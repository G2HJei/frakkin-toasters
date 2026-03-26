package xyz.zlatanov.frakkintoasters.board;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.Location.*;

public class CylonFleetBoard extends Board {
    public CylonFleetBoard() {
        super(Set.of(BASESTAR_BRIDGE,
                CYLON_FLEET_SPACE_1,
                CYLON_FLEET_SPACE_2,
                CYLON_FLEET_SPACE_3,
                CYLON_FLEET_SPACE_4,
                CYLON_FLEET_SPACE_5_6,
                CYLON_FLEET_SPACE_7_8));
    }
}
