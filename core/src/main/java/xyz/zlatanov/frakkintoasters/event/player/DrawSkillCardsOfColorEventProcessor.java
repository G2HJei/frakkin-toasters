package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.Collections;

public class DrawSkillCardsOfColorEventProcessor extends EventProcessor<DrawSkillCardsOfColorEvent> {

    // Only the cards left of that color, if fewer (rule 8.2.2.5).
    @Override
    public Followup process() {
        val count = Math.min(event.count(), game.decks().skillCardsLeft(event.color()));
        ops.player(player.number()).drawSkillCards(Collections.nCopies(count, event.color()));
        return Followup.NONE;
    }
}
