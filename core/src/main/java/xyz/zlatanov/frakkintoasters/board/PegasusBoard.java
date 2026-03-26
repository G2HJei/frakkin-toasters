package xyz.zlatanov.frakkintoasters.board;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.Location.*;

public class PegasusBoard extends Board {
    public PegasusBoard() {
        super(Set.of(PEGASUS_CIC, AIRLOCK, MAIN_BATTERIES, ENGINE_ROOM));
    }
}
