package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

public class PlaySkillsEventProcessor extends EventProcessor<PlaySkillsEvent> {
    @Override
    protected boolean isValid() {
        val inBrig = game.locate(player.character()) == BRIG;
        val cardsCount = event.skillCards().size();
        val maxCards = inBrig || !player.isHuman() ? 1
                : player.isInfiltrating() ? 2
                // : inDetention || isInfiltrating ? 2 todo after implementing Location.DETENTION
                : Integer.MAX_VALUE;
        return cardsCount <= maxCards;
    }

    @Override
    public Followup process() {
        game.activeSkillCheck().cards().addOnTop(event.skillCards());
        return Followup.NONE;
    }
}
