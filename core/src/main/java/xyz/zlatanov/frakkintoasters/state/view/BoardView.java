package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import java.util.List;
import java.util.Optional;

public interface BoardView {

    Optional<Location> locate(Character character);

    List<Character> charactersIn(Location location);
}
