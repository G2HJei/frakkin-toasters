package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.officials.GiveTitlesToNextInLineEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.MUTINEER;

public class PassMutineerCardEventProcessor extends EventProcessor<PassMutineerCardEvent> {

    @Override
    protected boolean isValid() {
        val recipient = event.recipientPlayerNumber();
        return isPlayer(recipient)
                && recipient != player.number()
                && game.player(recipient).isHuman()
                && player.loyaltyCards().cards().contains(MUTINEER);
    }

    @Override
    public Followup process() {
        val recipient = event.recipientPlayerNumber();
        ops.player(player.number()).giveLoyaltyCards(recipient, List.of(MUTINEER));
        // "That player must then draw one Mutiny Card and gives each Title Card he has to the player whose character is
        // highest in the order of succession for that title (excluding himself)."
        return all(new DrawMutinyCardEvent(recipient), new GiveTitlesToNextInLineEvent(recipient));
    }
}
