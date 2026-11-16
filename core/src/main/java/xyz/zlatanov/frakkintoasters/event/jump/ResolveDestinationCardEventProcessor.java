package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.ship.DrawAndDestroyCivilianShipsEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.MISJUMP;

public class ResolveDestinationCardEventProcessor extends EventProcessor<ResolveDestinationCardEvent> {

    @Override
    protected boolean isValid() {
        return event.card() != null;
    }

    @Override
    public Followup process() {
        val card = event.card();
        if (card == MISJUMP) {
            // "Draw 1 civilian ship and destroy it. Then discard this card and draw a new Destination Card to resolve."
            // The new card is drawn at once, without a choice, and it brings the steps that come after.
            ops.galactica().discardDestinationCard(MISJUMP);
            val next = ops.galactica().drawDestinationCards(1).getFirst();
            return all(single(new DrawAndDestroyCivilianShipsEvent(1)), single(new ResolveDestinationCardEvent(next)));
        }
        val distanceBefore = game.distanceTraveled();
        ops.galactica().reachDestination(card);
        return all(
                DestinationCardFollowups.effects(game, card),
                single(new ObjectiveInstructionsEvent(distanceBefore)),
                single(new ResetJumpPreparationEvent()),
                DestinationCardFollowups.afterReset(card));
    }
}
