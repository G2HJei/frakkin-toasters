package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.MUTINEER;

public class DealLoyaltyCardsEventProcessor extends EventProcessor<DealLoyaltyCardsEvent> {

    @Override
    protected boolean isValid() {
        return event.count() >= 0 && player.character() != null;
    }

    @Override
    public Followup process() {
        // Fewer cards are dealt when the deck has fewer left.
        val count = Math.min(event.count(), game.decks().loyaltyCardsLeft());
        if (count == 0) {
            return NONE;
        }
        val dealt = ops.player(player.number()).drawLoyaltyCards(count);
        // A Cylon who is dealt the card doesn't reveal it, he passes it on (rule 5.5.2.2): the Sleeper Agent phase does.
        return dealt.contains(MUTINEER) && player.isHuman()
                ? single(new RevealMutineerEvent(player.number(), !event.extra()))
                : NONE;
    }
}
