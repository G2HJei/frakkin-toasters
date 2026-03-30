package xyz.zlatanov.frakkintoasters.state.board;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

public class PegasusBoard extends Board {
    public PegasusBoard() {
        super(Set.of(PEGASUS_CIC, AIRLOCK, MAIN_BATTERIES, ENGINE_ROOM));
    }
}
