package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.MUTINEER;

public class GiveLoyaltyCardsEventProcessor extends EventProcessor<GiveLoyaltyCardsEvent> {

    @Override
    protected boolean isValid() {
        val recipient = event.recipientPlayerNumber();
        return isPlayer(recipient)
                && recipient != player.number()
                && game.player(recipient).isHuman()
                && !cardsToGive().isEmpty()
                && FinishSleeperAgentPhaseEventProcessor.canHandOffLoyaltyCards(game); // rule 6.1.1
    }

    @Override
    public Followup process() {
        ops.player(player.number()).giveLoyaltyCards(event.recipientPlayerNumber(), cardsToGive());
        return Followup.NONE;
    }

    // The Mutineer card is faceup, and it is passed on its own (PassMutineerCardEvent).
    private List<LoyaltyCard> cardsToGive() {
        return player.loyaltyCards().cards().stream()
                .filter(card -> card != MUTINEER)
                .toList();
    }
}
