package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.Collections;

public class DrawSkillCardsOfColorEventProcessor extends EventProcessor<DrawSkillCardsOfColorEvent> {

    @Override
    public Followup process() {
        ops.player(player.number()).drawSkillCards(Collections.nCopies(event.count(), event.color()));
        return Followup.NONE;
    }
}
