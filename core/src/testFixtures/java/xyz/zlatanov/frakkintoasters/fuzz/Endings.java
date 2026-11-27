package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.JumpingTheFleetEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEventProcessor;
import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.board.LocationsArea;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.track.BoardingParty;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.Arrays;
import java.util.Optional;

/**
 * The ending of a finished game, read from its final state: the rule's condition that holds. The event that ended the
 * game doesn't tell, because the engine checks the board after any event.
 */
public final class Endings {

    public enum Ending {
        FUEL_AT_0("Fuel at 0"),
        FOOD_AT_0("Food at 0"),
        MORALE_AT_0("Morale at 0"),
        POPULATION_AT_0("Population at 0"),
        GALACTICA_DESTROYED("6 damaged Galactica locations"),
        BOARDED("Centurions at the end of the Boarding Party track"),
        NO_CHARACTER_LEFT("No character left to choose"),
        KOBOL_HUMANS("Kobol, the humans win"),
        KOBOL_CYLONS("Kobol, a resource at 0 after the Personal Goals");

        public final String label;

        Ending(String label) {
            this.label = label;
        }
    }

    private static final int DAMAGED_LOCATIONS_TO_DESTROY_GALACTICA = 6;

    private Endings() {
    }

    public static Optional<Ending> classify(GameView game) {
        val winner = game.outcome();
        if (winner.isEmpty()) {
            return Optional.empty();
        }
        val atKobol = game.distanceTraveled() >= JumpingTheFleetEventProcessor.KOBOL_DISTANCE;
        val depleted = depleted(game);
        if (winner.get() == Team.HUMANS) {
            return atKobol && depleted.isEmpty() ? Optional.of(Ending.KOBOL_HUMANS) : Optional.empty();
        }
        if (atKobol && depleted.isPresent()) {
            return Optional.of(Ending.KOBOL_CYLONS);
        }
        if (depleted.isPresent()) {
            return depleted;
        }
        val galactica = game.boards().galactica();
        val damaged = LocationsArea.GALACTICA.locations().stream().filter(galactica::isDamaged).count();
        if (damaged >= DAMAGED_LOCATIONS_TO_DESTROY_GALACTICA) {
            return Optional.of(Ending.GALACTICA_DESTROYED);
        }
        if (galactica.boardingPartyTrack().containsValue(BoardingParty.HUMANS_LOSE)) {
            return Optional.of(Ending.BOARDED);
        }
        if (Arrays.stream(Character.values()).noneMatch(character -> SelectCharacterEventProcessor.canBeSelected(game,
                character))) {
            return Optional.of(Ending.NO_CHARACTER_LEFT);
        }
        return Optional.empty();
    }

    private static Optional<Ending> depleted(GameView game) {
        val galactica = game.boards().galactica();
        if (galactica.fuel() <= 0) {
            return Optional.of(Ending.FUEL_AT_0);
        }
        if (galactica.food() <= 0) {
            return Optional.of(Ending.FOOD_AT_0);
        }
        if (galactica.morale() <= 0) {
            return Optional.of(Ending.MORALE_AT_0);
        }
        if (galactica.population() <= 0) {
            return Optional.of(Ending.POPULATION_AT_0);
        }
        return Optional.empty();
    }
}
