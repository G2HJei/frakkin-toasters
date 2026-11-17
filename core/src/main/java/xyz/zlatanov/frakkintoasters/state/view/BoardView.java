package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface BoardView {

    Set<Location> locations();

    Map<Character, Location> characters();

    Optional<Location> locate(Character character);

    List<Character> charactersIn(Location location);
}
