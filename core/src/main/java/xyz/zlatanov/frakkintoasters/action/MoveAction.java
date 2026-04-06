package xyz.zlatanov.frakkintoasters.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.PilotableShip;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.Map;
import java.util.Objects;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER_MARK_VII;

public record MoveAction(int player, Location destination, SkillCard discardCard) implements Action {

    @Override
    public boolean isValid(Game game) {
        val validLocation = isEligibleLocation(game);
        val shouldDiscardCard = !Objects.equals(startingArea(game), targetArea());
        val isDiscardingCard = discardCard != null;
        return validLocation &&
                shouldDiscardCard == isDiscardingCard;
    }

    @Override
    public void apply(Game game) {
        if (discardCard != null) {
            game.player(player).skillCards().remove(discardCard);
            game.decks().discard(discardCard);
        }
        val playerCharacter = game.player(player).character();
        val currentLocation = game.locate(playerCharacter);
        val isPiloting = currentLocation.isSpaceLocation();
        val isStayingInSpace = destination.isSpaceLocation();
        if (isPiloting) {
            val ship = getShip(game);
            if (isStayingInSpace) {
                game.moveTo(destination, ship);
            } else {
                game.boards().galactica().addToReserves(ship);
                ((PilotableShip) ship).pilot(null);
                game.moveTo(destination, playerCharacter);
            }
        } else {
            game.moveTo(destination, playerCharacter);
        }
    }

    private boolean isEligibleLocation(Game game) {
        val revealedCylon = game.player(player).isRevealedCylon();
        val cylonLocation = destination.isCylonLocation();
        val hazardousLocation = destination.isHazardousLocation();
        val playerCharacter = game.player(player).character();
        val currentLocation = game.locate(playerCharacter);
        val isMovingToDifferentLocation = currentLocation != destination;
        return isMovingToDifferentLocation
                && !hazardousLocation
                && validDistance(game, currentLocation)
                && (revealedCylon == cylonLocation);
    }

    private boolean validDistance(Game game, Location currentLocation) {
        val isPiloting = currentLocation.isSpaceLocation();
        val isStayingInSpace = destination.isSpaceLocation();
        if (isPiloting && isStayingInSpace) {
            val distance = distanceLookupTable.get(currentLocation).get(destination);
            val maxDistance = 1 + (getShip(game).type() == VIPER_MARK_VII ? 1 : 0);
            return distance <= maxDistance;
        } else {
            return true;
        }
    }

    private String startingArea(Game game) {
        val location = game.locate(game.player(player).character());
        if (location == null) {
            return "Hi, Helo!";
        }
        return getLocationArea(location);
    }

    private String targetArea() {
        return getLocationArea(destination);
    }

    private String getLocationArea(Location location) {
        return LOCATION_AREAS.entrySet()
                .stream()
                .filter(es -> es.getValue().contains(location))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElseThrow(FrakCallTheAdmiralException::new);
    }

    private Ship getShip(Game game) {
        val playerCharacter = game.player(player).character();
        val currentLocation = game.locate(playerCharacter);
        return game.boards().galactica().shipsIn(currentLocation)
                .stream()
                .filter(s -> s instanceof PilotableShip
                        && ((PilotableShip) s).pilot() == playerCharacter)
                .findFirst()
                .orElseThrow();
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
