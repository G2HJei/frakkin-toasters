package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.character.SleeperAgentEvent;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.SLEEPER_AGENT;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public class SleeperAgentPhaseEventProcessor extends EventProcessor<SleeperAgentPhaseEvent> {

    @Override
    public Followup process() {
        val followups = new ArrayList<Followup>();
        for (val player : playersFrom(game, game.turn().currentPlayerNumber())) {
            if (player.character().type() == CYLON_LEADER) {
                ops.player(player.number()).drawMotiveCards(); // two cards, and no Loyalty Cards
            } else if (player.hasAbility(SLEEPER_AGENT)) {
                // Sharon "Boomer" Valerii is dealt 2, then moves to the "Brig". Revealed, her abilities are ignored and
                // "the unused Loyalty Card remains in the Loyalty deck" (rule 6.1.2.2).
                followups.add(all(new DealLoyaltyCardsEvent(player.number(), 2, false),
                        new SleeperAgentEvent(player.number())));
            } else {
                // Gaius Baltar's "Coward" second card is dealt at the start of the game only (rule A3.8.5).
                followups.add(single(new DealLoyaltyCardsEvent(player.number(), 1, false)));
            }
        }
        // What the Cylons do with the cards they were dealt depends on all of them.
        followups.add(single(new FinishSleeperAgentPhaseEvent()));
        return all(followups.toArray(Followup[]::new));
    }

}
