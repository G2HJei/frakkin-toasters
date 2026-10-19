package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.HashSet;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.RESTORE_ORDER;

public class PlayCardsBeforeSkillCheckEventProcessor extends EventProcessor<PlayCardsBeforeSkillCheckEvent> {

    //todo validate no duplicates and either reckless/restore order


    @Override
    protected boolean isValid() {
        val playableBeforeSkillCheck = event.skillCards().stream()
                .map(SkillCard::type)
                .allMatch(SkillCardType::playableBeforeSkillCheck);
        val noDuplicates = event.skillCards().size() == new HashSet<>(event.skillCards()).size();
        val recklessAndRestoreOrderCards = event.skillCards().stream()
                .filter(c -> c.type().reckless() || c.type() == RESTORE_ORDER)
                .count();
        return playableBeforeSkillCheck
                && noDuplicates
                && recklessAndRestoreOrderCards <= 1;
    }

    @Override
    public Followup process() {
        ops.skillCheck().playBeforeSkillCheck(event.playerNumber(), event.skillCards());
        return Followup.NONE;
    }
}
