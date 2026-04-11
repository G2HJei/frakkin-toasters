package xyz.zlatanov.frakkintoasters.state.board;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

@Getter
@Accessors(fluent = true)
public class PegasusBoard extends BattlestarBoard {

    public PegasusBoard() {
        super(Set.of(PEGASUS_CIC, AIRLOCK, MAIN_BATTERIES, ENGINE_ROOM));
    }
}
