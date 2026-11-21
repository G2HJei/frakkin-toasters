package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.LOYALTY;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.LOYALTY_NOT_CYLON;

public class GiveLoyaltyCardToPlayerEventProcessor extends EventProcessor<GiveLoyaltyCardToPlayerEvent> {

    @Override
    protected boolean isValid() {
        return isPlayer(event.targetPlayerNumber()) && event.card() != null && (from().isPresent() || isUnusedCylonCard());
    }

    @Override
    public Followup process() {
        ops.sandbox().giveLoyaltyCard(event.targetPlayerNumber(), event.card(), from().orElse(null));
        return Followup.NONE;
    }

    private Optional<DeckId> from() {
        if (game.decks().deck(LOYALTY).cards().contains(event.card())) {
            return Optional.of(LOYALTY);
        }
        if (game.decks().deck(LOYALTY_NOT_CYLON).cards().contains(event.card())) {
            return Optional.of(LOYALTY_NOT_CYLON);
        }
        return Optional.empty();
    }

    // The "You Are a Cylon" cards that aren't dealt into the Loyalty deck are in no deck: one nobody holds, revealed or
    // not, can be given.
    private boolean isUnusedCylonCard() {
        return event.card().isCylon()
                && !game.decks().deck(LOYALTY).discardedCards().contains(event.card())
                && game.players().stream().noneMatch(p -> p.loyaltyCards().cards().contains(event.card())
                || p.loyaltyCards().revealedCards().contains(event.card()));
    }
}
