package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.FAST_LEARNER;

public class FastLearnerIntoCheckEventProcessor extends EventProcessor<FastLearnerIntoCheckEvent> {

    @Override
    protected boolean isValid() {
        val skillCheck = game.activeSkillCheck();
        return skillCheck != null
                && skillCheck.result() == null
                && skillCheck.totalStrength() == null
                && player.hasAbility(FAST_LEARNER)
                && event.cards() != null
                && !event.cards().isEmpty()
                && holdsSkillCards(event.cards());
    }

    @Override
    public Followup process() {
        ops.skillCheck().playCards(player.number(), event.cards());
        if (game.activeSkillCheck().investigated()) {
            ops.skillCheck().show(player.number(), event.cards()); // as the cards anyone plays in
        }
        return NONE;
    }
}
