package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class PlayFromDestinyDeckEventProcessor extends EventProcessor<PlayFromDestinyDeckEvent> {

    @Override
    public Followup process() {
        val destinyCards = game.decks().destiny().draw(2);
        game.activeSkillCheck().cards().addOnTop(destinyCards);
        return Followup.NONE;
    }
}
