package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.action.ViperActivationRoundEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.COMBAT_VETERAN;

public class CombatVeteranEventProcessor extends SkillCardEventProcessor<CombatVeteranEvent> {

    public static boolean canPlay(GameView game) {
        return canActivateUnmanned(game);
    }

    @Override
    public SkillCardType type() {
        return COMBAT_VETERAN;
    }

    // An unmanned viper, never one another player is piloting (rule 11.1). One in the "Reserves" can only be launched
    // (rule 11.1.1), so the launch is its first activation.
    @Override
    protected boolean isValidPlay() {
        return isValidActivation(game, event.first(), fighter -> fighter.pilot() == null, true);
    }

    @Override
    protected Followup resolve() {
        int viperId = event.first().launchShipType() != null ? launchedViper() : event.first().humanFighterId();
        return allOf(activate(ops, player.number(), event.first()),
                single(new ViperActivationRoundEvent(player.number(), viperId, 2, false)));
    }

    // The viper a launch from the "Reserves" takes: the one with the lowest id of that type, as GalacticaOps does.
    private int launchedViper() {
        return galacticaBoard.reserves().stream()
                .filter(ship -> ship.getClass() == event.first().launchShipType().shipClass())
                .findFirst()
                .orElseThrow()
                .id();
    }
}
