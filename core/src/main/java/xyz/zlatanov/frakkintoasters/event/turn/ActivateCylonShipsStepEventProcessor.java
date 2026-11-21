package xyz.zlatanov.frakkintoasters.event.turn;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.character.ModificationsEventProcessor;
import xyz.zlatanov.frakkintoasters.event.crisis.CrisisCardFollowups;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.GameStep.ACTIVATE_CYLON_SHIPS;

public class ActivateCylonShipsStepEventProcessor extends EventProcessor<ActivateCylonShipsStepEvent> {

    @Override
    public Followup process() {
        if (game.turn().endedEarly() || !game.currentPlayer().isHuman()) {
            // Revealed Cylons have no Activate Cylon Ships step (rules 8.6, 6.1.3). A Crisis Card chosen at "Caprica"
            // has none either.
            return NONE;
        }
        ops.changeStep(ACTIVATE_CYLON_SHIPS);
        val cards = game.turn().crisisCards();
        if (cards.isEmpty()) {
            // No Crisis step, e.g. in the "Brig", so no card shows an icon. "Modifications" still may pick one, at the
            // start of any player's step.
            return ModificationsEventProcessor.offerWithoutCard(game);
        }
        // The icon of the card that was drawn last (rule 8.6).
        // "Modifications" may pick another activation instead.
        val activation = ModificationsEventProcessor.offer(game, CrisisCardFollowups.activation(cards.getLast().activation()));
        if (!game.turn().returnsToCrisisStep()) {
            return activation;
        }
        ops.returnToCrisisStep(false);
        return all(activation, single(new CrisisStepEvent(true)));
    }
}
