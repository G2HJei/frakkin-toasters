package xyz.zlatanov.frakkintoasters.board;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.Pursuit;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.Location.*;
import static xyz.zlatanov.frakkintoasters.Pursuit.START;

@Getter
@Accessors(fluent = true)
public class CylonFleetBoard extends Board {

    private Pursuit pursuitTrack = START;

    public CylonFleetBoard() {
        super(Set.of(BASESTAR_BRIDGE,
                CYLON_FLEET_SPACE_1,
                CYLON_FLEET_SPACE_2,
                CYLON_FLEET_SPACE_3,
                CYLON_FLEET_SPACE_4,
                CYLON_FLEET_SPACE_5_6,
                CYLON_FLEET_SPACE_7_8));
    }

    public void advancePursuit() {
        val current = pursuitTrack.ordinal();
        val autoAttack = Pursuit.values().length - 1;
        val next = current == autoAttack ? 0 : current + 1;
        pursuitTrack = Pursuit.values()[next];
    }
}
