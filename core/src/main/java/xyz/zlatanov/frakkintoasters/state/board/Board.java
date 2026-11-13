package xyz.zlatanov.frakkintoasters.state.board;

import xyz.zlatanov.frakkintoasters.state.Mutates;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.view.BoardView;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface Board extends BoardView {

    Set<Location> locations();

    Map<Character, Location> characters();

    @Mutates
    void place(Location to, Character character);

    @Mutates
    void remove(Character character);

    @Override
    default Optional<Location> locate(Character character) {
        return Optional.ofNullable(characters().get(character));
    }

    @Override
    default List<Character> charactersIn(Location location) {
        return characters().entrySet()
                .stream()
                .filter(es -> es.getValue() == location)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

}
