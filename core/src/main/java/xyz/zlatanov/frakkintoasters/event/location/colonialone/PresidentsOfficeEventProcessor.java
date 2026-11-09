package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;

import java.util.Collections;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;

public class PresidentsOfficeEventProcessor extends LocationActionProcessor<PresidentsOfficeEvent> {

    // Only the politics cards left, if fewer than 2 (rule 8.2.2.5).
    @Override
    public Followup process() {
        val count = Math.min(2, game.decks().skillCardsLeft(POLITICS));
        ops.player(event.playerNumber()).drawSkillCards(Collections.nCopies(count, POLITICS));
        return Followup.NONE;
    }
}
