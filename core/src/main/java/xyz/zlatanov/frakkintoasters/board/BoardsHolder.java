package xyz.zlatanov.frakkintoasters.board;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

@Builder
@Getter
@Accessors(fluent = true)
public class BoardsHolder {
    private GalacticaBoard  galactica;
    private PegasusBoard    pegasus;
    private CylonFleetBoard cylonFleet;
}
