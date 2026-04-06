package xyz.zlatanov.frakkintoasters.event.player;

import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.PilotableShip;
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
    public void apply(Game game) {
        if (discardCard != null) {
            game.player(playerNumber).skillCards().remove(discardCard);
            game.decks().discard(discardCard);
        }
        val playerCharacter = playerCharacter(game);
        val isPiloting = currentLocation(game).isSpaceLocation();
        val isStayingInSpace = destination.isSpaceLocation();
        if (isPiloting) {
            val ship = pilotedShip(game);
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
    }

    private boolean isEligibleLocation(Game game) {
        val revealedCylon = game.player(playerNumber).isRevealedCylon();
        val cylonLocation = destination.isCylonLocation();
        val hazardousLocation = destination.isHazardousLocation();
        val currentLocation = game.locate(playerCharacter(game));
        val isMovingToDifferentLocation = currentLocation != destination;
        val shipToSpaceMovement = !currentLocation.isSpaceLocation() && destination.isSpaceLocation();
        return isMovingToDifferentLocation
                && !shipToSpaceMovement
                && !hazardousLocation
                && validDistance(game, currentLocation)
                && (revealedCylon == cylonLocation);
    }

    private boolean validDistance(Game game, Location currentLocation) {
        val isPiloting = currentLocation.isSpaceLocation();
        val isStayingInSpace = destination.isSpaceLocation();
        if (isPiloting && isStayingInSpace) {
            val distance = distanceLookupTable.get(currentLocation).get(destination);
            val maxDistance = 1 + (pilotedShip(game).type() == VIPER_MARK_VII ? 1 : 0);
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


    private PilotableShip pilotedShip(Game game) {
        val playerCharacter = playerCharacter(game);
        val galacticaBoard = game.boards().galactica();
        return LOCATION_AREAS.get("Galactica space")
                .stream()
                .map(galacticaBoard::shipsIn)
                .flatMap(Collection::stream)
                .filter(s -> s instanceof PilotableShip
                        && ((PilotableShip) s).pilot() == playerCharacter)
                .map(PilotableShip.class::cast)
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
