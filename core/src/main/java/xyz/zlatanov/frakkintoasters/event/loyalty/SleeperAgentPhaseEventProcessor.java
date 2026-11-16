package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;
import static xyz.zlatanov.frakkintoasters.event.loyalty.CreateLoyaltyDeckEventProcessor.CHARACTERS_WITH_EXTRA_CARDS;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public class SleeperAgentPhaseEventProcessor extends EventProcessor<SleeperAgentPhaseEvent> {

    @Override
    public Followup process() {
        val followups = new ArrayList<Followup>();
        for (val player : playersFrom(game, game.turn().currentPlayerNumber())) {
            if (player.character().type() == CYLON_LEADER) {
                ops.player(player.number()).drawMotiveCards(); // two cards, and no Loyalty Cards
            } else {
                followups.add(single(new DealLoyaltyCardsEvent(player.number(), loyaltyCards(player), false)));
            }
        }
        // What the Cylons do with the cards they were dealt depends on all of them.
        followups.add(single(new FinishSleeperAgentPhaseEvent()));
        return all(followups.toArray(Followup[]::new));
    }

    // "The unused Loyalty Card remains in the Loyalty deck" of a revealed Sharon "Boomer" Valerii (rule 6.1.2.2).
    private int loyaltyCards(PlayerView player) {
        return CHARACTERS_WITH_EXTRA_CARDS.contains(player.character()) && !player.isRevealedCylon() ? 2 : 1;
    }
}
