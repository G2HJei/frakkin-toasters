package xyz.zlatanov.frakkintoasters.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.Pilotable;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.Map;
import java.util.Objects;

import static xyz.zlatanov.frakkintoasters.state.board.Location.LOCATION_AREAS;

public record MoveAction(int player, Location location, SkillCard discardCard) implements Action {

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
        val isStayingInSpace = location.isSpaceLocation();
        if (isPiloting) {
            val ship = getShip(game);
            if (isStayingInSpace) {
                game.moveTo(location, ship);
            } else {
                game.boards().galactica().addToReserves(ship);
                ((Pilotable) ship).pilot(null);
                game.moveTo(location, playerCharacter);
            }
        } else {
            game.moveTo(location, playerCharacter);
        }
    }

    private boolean isEligibleLocation(Game game) {
        val revealedCylon = game.player(player).isRevealedCylon();
        val cylonLocation = location.isCylonLocation();
        val hazardousLocation = location.isHazardousLocation();
        return !hazardousLocation
                && (revealedCylon == cylonLocation);
    }

    private String startingArea(Game game) {
        val location = game.locate(game.player(player).character());
        if (location == null) {
            return "Hi, Helo!";
        }
        return getLocationArea(location);
    }

    private String targetArea() {
        return getLocationArea(location);
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
                .filter(s -> s instanceof Pilotable
                        && ((Pilotable) s).pilot() == playerCharacter)
                .findFirst()
                .orElseThrow();
    }
}
