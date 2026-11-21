package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEventProcessor;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BUILD_NUKE;

public class BuildNukeEventProcessor extends SkillCardEventProcessor<BuildNukeEvent> {

    // The Admiral may not hold more than 3 (rule 11.6.1), the number of tokens in the game (A2.5.5).
    public static final int MAX_NUKES = 3;

    public static boolean canPlay(GameView game) {
        return game.nukes() < MAX_NUKES;
    }

    @Override
    public SkillCardType type() {
        return BUILD_NUKE;
    }

    @Override
    protected boolean isValidPlay() {
        return canPlay(game);
    }

    @Override
    protected Followup resolve() {
        ops.gainNuke();
        return NONE;
    }
}
