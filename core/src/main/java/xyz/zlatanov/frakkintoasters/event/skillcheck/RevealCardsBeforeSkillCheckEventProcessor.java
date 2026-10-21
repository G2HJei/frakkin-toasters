package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.GUTS_AND_INITIATIVE;

public class RevealCardsBeforeSkillCheckEventProcessor extends EventProcessor<RevealCardsBeforeSkillCheckEvent> {

    @Override
    public Followup process() {
        if (gutsAndInitiativeWasPlayed()) {
            return NONE;
        }
        return all(new PlayFromDestinyDeckEvent());
    }

    private boolean gutsAndInitiativeWasPlayed() {
        return game.activeSkillCheck()
                .playedBeforeSkillCheck()
                .stream()
                .map(SkillCard::type)
                .anyMatch(GUTS_AND_INITIATIVE::equals);
    }
}
