package xyz.zlatanov.frakkintoasters.state.board;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.Mutates;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.view.PegasusBoardView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.PEGASUS;

@Getter //todo replace getters with explicit methods for View interface usage to ensure no modification is possible
@Accessors(fluent = true)
public class PegasusBoard implements BattlestarBoard, PegasusBoardView {

    private final Set<Location>            locations           = new HashSet<>(PEGASUS.locations());
    private final Set<Location>            damagedLocations    = new HashSet<>();
    private final Map<Character, Location> characters          = new HashMap<>();
    @Setter(onMethod_ = @Mutates)
    private       boolean                  engineRoomActivated = false;

}
