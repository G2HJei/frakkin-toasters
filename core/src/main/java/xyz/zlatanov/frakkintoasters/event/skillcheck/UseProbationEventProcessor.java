package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.PROBATION;

public class UseProbationEventProcessor extends EventProcessor<UseProbationEvent> {

    // Only ever an option, right after the holder put his cards in, so his cards are the top of the pile.
    @Override
    protected boolean isValid() {
        return player.character() != null
                && player.character() == game.president()
                && game.quorumCardHolder(PROBATION).equals(Optional.of(event.holderPlayerNumber()))
                && game.activeSkillCheck() != null
                && event.count() >= 1 && event.count() <= game.activeSkillCheck().cards().size();
    }

    @Override
    public Followup process() {
        val cards = game.activeSkillCheck().cards().cards().subList(0, event.count());
        ops.skillCheck().lookAt(player.number(), event.holderPlayerNumber(), cards);
        ops.titles().discardFromPlay(PROBATION);
        return Followup.NONE;
    }
}
