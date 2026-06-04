package xyz.zlatanov.frakkintoasters.state.board;

import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.PEGASUS;

@Getter
@Accessors(fluent = true)
public class PegasusBoard implements BattlestarBoard {

    private final Set<Location>            locations        = new HashSet<>(PEGASUS.locations());
    private final Set<Location>            damagedLocations = new HashSet<>();
    private final Map<Character, Location> characters       = new HashMap<>();
}
