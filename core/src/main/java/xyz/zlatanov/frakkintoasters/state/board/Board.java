package xyz.zlatanov.frakkintoasters.state.board;

import lombok.val;
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

    default void place(Location to, Character... characterToPlace) {
        assert locations().contains(to) && !to.isSpaceLocation();
        for (val c : characterToPlace) {
            characters().put(c, to);
        }
    }

    @Override
    default Optional<Location> locate(Character character) {
        return Optional.ofNullable(characters().get(character));
    }

    default void remove(Character character) {
        remove(List.of(character));
    }

    default void remove(List<Character> charactersToRemove) {
        charactersToRemove.forEach(c -> characters().remove(c));
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
