package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.LocationsArea;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;
import xyz.zlatanov.frakkintoasters.state.ship.ViperMarkVII;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA_SPACE;

public class MoveEventProcessor extends EventProcessor<MoveEvent> {

    private SkillCard discardCard;
    private Location  destination;

    @Override
    protected void init() {
        discardCard = event.discardCard();
        destination = event.destination();
    }

    @Override
    public boolean isValid() {
        val isEligibleLocation = isEligibleLocation();
        val shouldDiscardCard = !Objects.equals(startingArea(), targetArea());
        val isDiscardingCard = discardCard != null;
        return isEligibleLocation &&
                shouldDiscardCard == isDiscardingCard;
    }

    @Override
    public Followup process() {
        if (discardCard != null) {
            player.skillCards().remove(discardCard);
            game.decks().discard(discardCard);
        }
        val playerCharacter = player.character();
        val isPiloting = game.locate(playerCharacter).isSpaceLocation();
        val isStayingInSpace = destination.isSpaceLocation();
        if (isPiloting) {
            val ship = humanFighter();
            if (isStayingInSpace) {
                game.moveTo(destination, ship);
            } else {
                galacticaBoard.addToReserves(ship);
                ship.pilot(null);
                game.moveTo(destination, playerCharacter);
            }
        } else {
            game.moveTo(destination, playerCharacter);
        }
        return Followup.NONE;
    }

    private boolean isEligibleLocation() {
        val isHuman = game.player(event.playerNumber()).isHuman();
        val cylonLocation = destination.isCylonLocation();
        val hazardousLocation = destination.isHazardousLocation();
        val currentLocation = currentLocation();
        val isMovingToDifferentLocation = currentLocation != destination;
        val shipToSpaceMovement = !currentLocation.isSpaceLocation() && destination.isSpaceLocation();
        return isMovingToDifferentLocation
                && !shipToSpaceMovement
                && !hazardousLocation
                && validSpaceDistance(currentLocation)
                && isHuman != cylonLocation;
    }

    private boolean validSpaceDistance(Location currentLocation) {
        val isPiloting = currentLocation.isSpaceLocation();
        val isStayingInSpace = destination.isSpaceLocation();
        if (isPiloting && isStayingInSpace) {
            val distance = distanceLookupTable.get(currentLocation).get(destination);
            val maxDistance = 1 + (humanFighter() instanceof ViperMarkVII ? 1 : 0);
            return distance <= maxDistance;
        } else {
            return true;
        }
    }

    private LocationsArea startingArea() {
        val currLocation = currentLocation();
        if (currLocation == null) {
            return null;
        }
        return getLocationArea(currLocation);
    }

    private LocationsArea targetArea() {
        return getLocationArea(destination);
    }


    private HumanFighter humanFighter() {
        val playerCharacter = player.character();
        return GALACTICA_SPACE.locations()
                .stream()
                .map(galacticaBoard::shipsIn)
                .flatMap(Collection::stream)
                .filter(s -> s instanceof HumanFighter
                        && ((HumanFighter) s).pilot() == playerCharacter)
                .map(HumanFighter.class::cast)
                .findFirst()
                .orElseThrow();
    }

    private LocationsArea getLocationArea(Location location) {
        return Arrays.stream(LocationsArea.values())
                .filter(a -> a.locations().contains(location))
                .findFirst()
                .orElseThrow(FrakCallTheAdmiralException::new);
    }

    private Location currentLocation() {
        return game.locate(player.character());
    }

    private static final Map<Location, Map<Location, Integer>> distanceLookupTable = Map.of(
            GALACTICA_SPACE_2_OCLOCK, Map.of(
                    GALACTICA_SPACE_4_OCLOCK, 1,
                    GALACTICA_SPACE_6_OCLOCK, 2,
                    GALACTICA_SPACE_8_OCLOCK, 3,
                    GALACTICA_SPACE_10_OCLOCK, 2,
                    GALACTICA_SPACE_12_OCLOCK, 1),
            GALACTICA_SPACE_4_OCLOCK, Map.of(
                    GALACTICA_SPACE_6_OCLOCK, 1,
                    GALACTICA_SPACE_8_OCLOCK, 2,
                    GALACTICA_SPACE_10_OCLOCK, 3,
                    GALACTICA_SPACE_12_OCLOCK, 2,
                    GALACTICA_SPACE_2_OCLOCK, 1),
            GALACTICA_SPACE_6_OCLOCK, Map.of(
                    GALACTICA_SPACE_8_OCLOCK, 1,
                    GALACTICA_SPACE_10_OCLOCK, 2,
                    GALACTICA_SPACE_12_OCLOCK, 3,
                    GALACTICA_SPACE_2_OCLOCK, 2,
                    GALACTICA_SPACE_4_OCLOCK, 1),
            GALACTICA_SPACE_8_OCLOCK, Map.of(
                    GALACTICA_SPACE_10_OCLOCK, 1,
                    GALACTICA_SPACE_12_OCLOCK, 2,
                    GALACTICA_SPACE_2_OCLOCK, 3,
                    GALACTICA_SPACE_4_OCLOCK, 2,
                    GALACTICA_SPACE_6_OCLOCK, 1),
            GALACTICA_SPACE_10_OCLOCK, Map.of(
                    GALACTICA_SPACE_12_OCLOCK, 1,
                    GALACTICA_SPACE_2_OCLOCK, 2,
                    GALACTICA_SPACE_4_OCLOCK, 3,
                    GALACTICA_SPACE_6_OCLOCK, 2,
                    GALACTICA_SPACE_8_OCLOCK, 1),
            GALACTICA_SPACE_12_OCLOCK, Map.of(
                    GALACTICA_SPACE_2_OCLOCK, 1,
                    GALACTICA_SPACE_4_OCLOCK, 2,
                    GALACTICA_SPACE_6_OCLOCK, 3,
                    GALACTICA_SPACE_8_OCLOCK, 2,
                    GALACTICA_SPACE_10_OCLOCK, 1)
    );
}
