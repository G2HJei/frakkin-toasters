package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

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
        return Followup.NONE;
    }
}
