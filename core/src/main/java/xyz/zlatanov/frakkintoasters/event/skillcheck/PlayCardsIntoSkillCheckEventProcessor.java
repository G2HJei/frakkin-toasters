package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;

import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.PROBATION;

public class PlayCardsIntoSkillCheckEventProcessor extends EventProcessor<PlayCardsIntoSkillCheckEvent> {
    @Override
    protected boolean isValid() {
        val inBrig = game.locate(player.character()).filter(BRIG::equals).isPresent();
        val cardsCount = event.skillCards().size();
        val maxCards = inBrig || !player.isHuman() ? 1
                : player.isInfiltrating() ? 2
                : Integer.MAX_VALUE;
        return holdsSkillCards(event.skillCards()) && cardsCount <= maxCards;
    }

    @Override
    public Followup process() {
        ops.skillCheck().playCards(player.number(), event.skillCards());
        if (game.activeSkillCheck().investigated() && !event.skillCards().isEmpty()) {
            // The cards go into the pile like any other, and everyone sees them (the Destiny cards are dealt face down).
            ops.skillCheck().show(player.number(), event.skillCards());
        }
        return probation();
    }

    // The President may then use "Probation" to look at the cards its holder put in (rule A2.3.13.2), unless he holds
    // it himself.
    private Followup probation() {
        if (event.skillCards().isEmpty()
                || !game.quorumCardHolder(PROBATION).equals(Optional.of(player.number()))
                || game.president() == null
                || game.president() == player.character()) {
            return Followup.NONE;
        }
        val president = game.playerNumber(game.president());
        return one(new UseProbationEvent(president, player.number(), event.skillCards().size()), new NoOpEvent(president));
    }
}
