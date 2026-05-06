package xyz.zlatanov.frakkintoasters.event.player;

import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER_MARK_VII;

@Accessors(fluent = true)
public record MoveEvent(int playerNumber, Location destination, SkillCard discardCard) implements PlayerEvent {

    @Override
    public boolean isValid(Game game) {
        val isEligibleLocation = isEligibleLocation(game);
        val shouldDiscardCard = !Objects.equals(startingArea(game), targetArea());
        val isDiscardingCard = discardCard != null;
        return isEligibleLocation &&
                shouldDiscardCard == isDiscardingCard;
    }

    @Override
    public Followup apply(Game game) {
        if (discardCard != null) {
            game.player(playerNumber).skillCards().remove(discardCard);
            game.decks().discard(discardCard);
        }
        val playerCharacter = playerCharacter(game);
        val isPiloting = currentLocation(game).isSpaceLocation();
        val isStayingInSpace = destination.isSpaceLocation();
        if (isPiloting) {
            val ship = humanFighter(game);
            if (isStayingInSpace) {
                game.moveTo(destination, ship);
            } else {
                game.boards().galactica().addToReserves(ship);
                ship.pilot(null);
                game.moveTo(destination, playerCharacter);
            }
        } else {
            game.moveTo(destination, playerCharacter);
        }
        return Followup.NONE;
    }

    private boolean isEligibleLocation(Game game) {
        val isHuman = game.player(playerNumber).isHuman();
        val cylonLocation = destination.isCylonLocation();
        val hazardousLocation = destination.isHazardousLocation();
        val currentLocation = game.locate(playerCharacter(game));
        val isMovingToDifferentLocation = currentLocation != destination;
        val shipToSpaceMovement = !currentLocation.isSpaceLocation() && destination.isSpaceLocation();
        return isMovingToDifferentLocation
                && !shipToSpaceMovement
                && !hazardousLocation
                && validSpaceDistance(game, currentLocation)
                && isHuman != cylonLocation;
    }

    private boolean validSpaceDistance(Game game, Location currentLocation) {
        val isPiloting = currentLocation.isSpaceLocation();
        val isStayingInSpace = destination.isSpaceLocation();
        if (isPiloting && isStayingInSpace) {
            val distance = distanceLookupTable.get(currentLocation).get(destination);
            val maxDistance = 1 + (humanFighter(game).type() == VIPER_MARK_VII ? 1 : 0);
            return distance <= maxDistance;
        } else {
            return true;
        }
    }

    private String startingArea(Game game) {
        val currLocation = currentLocation(game);
        if (currLocation == null) {
            return "Hi, Helo!";
        }
        return getLocationArea(currLocation);
    }

    private String targetArea() {
        return getLocationArea(destination);
    }


    private HumanFighter humanFighter(Game game) {
        val playerCharacter = playerCharacter(game);
        val galacticaBoard = game.boards().galactica();
        return LOCATION_AREAS.get("Galactica space")
                .stream()
                .map(galacticaBoard::shipsIn)
                .flatMap(Collection::stream)
                .filter(s -> s instanceof HumanFighter
                        && ((HumanFighter) s).pilot() == playerCharacter)
                .map(HumanFighter.class::cast)
                .findFirst()
                .orElseThrow();
    }

    private String getLocationArea(Location location) {
        return LOCATION_AREAS.entrySet()
                .stream()
                .filter(es -> es.getValue().contains(location))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElseThrow(FrakCallTheAdmiralException::new);
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
