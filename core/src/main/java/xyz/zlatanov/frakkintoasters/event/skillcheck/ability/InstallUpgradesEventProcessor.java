package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.ENGINEERING;

public class InstallUpgradesEventProcessor extends EventProcessor<InstallUpgradesEvent> {

    @Override
    public Followup process() {
        ops.player(game.currentPlayerNumber())
                .drawSkillCards(event.skillCheckPassed()
                        ? List.of(ENGINEERING, ENGINEERING)
                        : List.of(ENGINEERING));
        return Followup.NONE;
    }
}
