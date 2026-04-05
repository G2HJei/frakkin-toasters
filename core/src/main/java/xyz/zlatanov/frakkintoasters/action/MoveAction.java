package xyz.zlatanov.frakkintoasters.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.state.board.Location.LOCATIONS_BY_SHIP;

public record MoveAction(int player, Location location, SkillCard discardCard) implements Action {

    @Override
    public boolean isValid(Game game) {
        val validLocation = isEligibleLocation(game);
        val shouldDiscardCard = startingShipIndex(game) != targetShipIndex();
        val isDiscardingCard = discardCard != null;
        return validLocation &&
                shouldDiscardCard == isDiscardingCard;
    }

    private boolean isEligibleLocation(Game game) {
        val revealedCylon = game.player(player).isRevealedCylon();
        val cylonLocation = location.isCylonLocation();
        val hazardousLocation = location.isHazardousLocation();
        return !hazardousLocation
                && (revealedCylon == cylonLocation);
    }

    @Override
    public void apply(Game game) {
        if (discardCard != null) {
            game.player(player).skillCards().remove(discardCard);
            game.decks().discard(discardCard);
        }
        game.moveTo(location, game.player(player).character());
    }

    private int startingShipIndex(Game game) {
        val location = game.locate(game.player(player).character());
        if (location == null) {
            return -1;
        }
        return getShipIndex(location);
    }

    private int targetShipIndex() {
        return getShipIndex(location);
    }

    private int getShipIndex(Location location) {
        for (int shipIndex = 0; shipIndex < LOCATIONS_BY_SHIP.size(); shipIndex++) {
            var shipLocations = LOCATIONS_BY_SHIP.get(shipIndex);
            if (shipLocations.contains(location)) {
                return shipIndex;
            }
        }
        throw new FrakCallTheAdmiralException();
    }
}
