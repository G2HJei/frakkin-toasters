package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.event.character.CharacterPenalties;
import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import java.util.stream.Stream;

public class DamageHumanFighterEventProcessor extends EventProcessor<DamageHumanFighterEvent> {

    @Override
    protected boolean isValid() {
        return event.shipId() != null && canBeDamaged(game, event.shipId());
    }

    @Override
    public Followup process() {
        // "Forced to Eject": Brendan "Hot Dog" Costanza's viper is destroyed instead.
        if (CharacterPenalties.destroyedInsteadOfDamaged(game, event.shipId())) {
            ops.ship(event.shipId()).destroy();
        } else {
            ops.ship(event.shipId()).damage();
        }
        return Followup.NONE;
    }

    // A viper in a space area or in the "Reserves", e.g. for "Dogfight".
    public static boolean canBeDamaged(GameView game, int shipId) {
        val galactica = game.boards().galactica();
        return Stream.concat(
                        galactica.shipsInSpace(HumanFighterView.class).stream(),
                        galactica.reserves().stream().filter(HumanFighterView.class::isInstance))
                .anyMatch(fighter -> fighter.id() == shipId);
    }
}
