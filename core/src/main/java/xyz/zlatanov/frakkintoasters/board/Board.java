package xyz.zlatanov.frakkintoasters.board;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.Character;
import xyz.zlatanov.frakkintoasters.Location;
import xyz.zlatanov.frakkintoasters.exception.InvalidMoveLocationException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class Board {

    private final Set<Location>            locations;
    private final Map<Character, Location> characters = new HashMap<>();

    public Set<Location> locations() {
        return new HashSet<>(locations);
    }

    public void moveTo(Location location, Character... characterToPlace) {
        moveTo(location, Set.of(characterToPlace));
    }

    public void moveTo(Location location, Set<Character> characterToPlace) {
        if (!locations.contains(location)) {
            throw new InvalidMoveLocationException();
        }
        characterToPlace.forEach(c -> characters.put(c, location));
    }

    public Location locate(Character character) {
        return characters.get(character);
    }

    public void remove(Character... charactersToRemove) {
        remove(Set.of(charactersToRemove));
    }

    public void remove(Set<Character> charactersToRemove) {
        charactersToRemove.forEach(characters::remove);
    }

    public Set<Character> charactersIn(Location... lookupLocations) {
        return charactersIn(Set.of(lookupLocations));
    }

    public Set<Character> charactersIn(Set<Location> lookupLocations) {
        return characters.entrySet()
                .stream()
                .filter(es -> lookupLocations.contains(es.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    protected void removeLocations(Location... locationsToRemove) {
        for (val loc : locationsToRemove) {
            locations.remove(loc);
        }
    }
}
