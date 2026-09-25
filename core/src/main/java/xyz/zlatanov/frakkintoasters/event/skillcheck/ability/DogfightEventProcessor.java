package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.DamageHumanFighterEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class DogfightEventProcessor extends EventProcessor<DogfightEvent> {

    @Override
    public Followup process() {
        val removedCard = event.removedCard();
        if (removedCard == null) {
            return Followup.NONE;
        }
        game.activeSkillCheck().remove(removedCard);
        game.decks().discard(removedCard);
        return single(new DamageHumanFighterEvent(event.viperId()));
    }
}
