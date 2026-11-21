package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.endgame.MotiveCards;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;

public class RevealMotiveCardEventProcessor extends EventProcessor<RevealMotiveCardEvent> {

    @Override
    protected boolean isValid() {
        // a card he holds face down: a revealed one is no longer among his cards
        return event.card() != null
                && player.motiveCards().cards().contains(event.card())
                && MotiveCards.canReveal(game, player, event.card());
    }

    @Override
    public Followup process() {
        ops.player(player.number()).revealMotiveCard(event.card());
        return NONE;
    }
}
