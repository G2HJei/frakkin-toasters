package xyz.zlatanov.frakkintoasters.board;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.Character;
import xyz.zlatanov.frakkintoasters.Location;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class Board {
    private final Set<Location> locations;
    private final Map<Character, Location> characters = new HashMap<>();

    public Set<Location> locations() {
        return new HashSet<>(locations);
    }


    public void moveTo(Location location, Set<Character> characterToPlace) {
        moveTo(location, characterToPlace.toArray(new Character[]{}));
    }

    public void moveTo(Location location, Character... characterToPlace) {
        Arrays.stream(characterToPlace).forEach(c -> characters.put(c, location));
    }

    public Location locate(Character character) {
        return characters.get(character);
    }

    protected void removeLocations(Location... locationsToRemove) {
        for (val loc : locationsToRemove) {
            locations.remove(loc);
        }
    }

    protected Set<Character> charactersIn(Set<Location> lookupLocations) {
        return charactersIn(lookupLocations.toArray(new Location[]{}));
    }


    protected Set<Character> charactersIn(Location... lookupLocations) {
        val lookupLocationsList = Arrays.asList(lookupLocations);
        return characters.entrySet()
                .stream()
                .filter(es -> lookupLocationsList.contains(es.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
