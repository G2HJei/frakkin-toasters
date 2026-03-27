package xyz.zlatanov.frakkintoasters.board;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.Character;
import xyz.zlatanov.frakkintoasters.Location;
import xyz.zlatanov.frakkintoasters.exception.InvalidMoveLocationException;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class Board {

    protected final Set<Location>            locations;
    private final   Map<Character, Location> characters = new HashMap<>();

    public Set<Location> locations() {
        return new HashSet<>(locations);
    }

    public void place(Location to, Character... characterToPlace) {
        if (!locations.contains(to) || to.isSpaceLocation()) {
            throw new InvalidMoveLocationException();
        }
        for (val c : characterToPlace) {
            characters.put(c, to);
        }
    }

    public Location locate(Character character) {
        return characters.get(character);
    }

    public void remove(Character character) {
        remove(List.of(character));
    }

    public void remove(List<Character> charactersToRemove) {
        charactersToRemove.forEach(characters::remove);
    }

    public List<Character> charactersIn(Location location) {
        return characters.entrySet()
                .stream()
                .filter(es -> es.getValue() == location)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

}
