package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.action.ViperActivationRoundEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.MAXIMUM_FIREPOWER;

public class MaximumFirepowerEventProcessor extends SkillCardEventProcessor<MaximumFirepowerEvent> {

    // He pilots a viper with a Cylon ship to attack in its space area.
    public static boolean canPlay(GameView game, PlayerView player) {
        return game.boards().galactica().pilotedFighter(player.character())
                .filter(fighter -> !ActivateViperEventProcessor.activations(game, fighter, true).isEmpty())
                .isPresent();
    }

    @Override
    public SkillCardType type() {
        return MAXIMUM_FIREPOWER;
    }

    @Override
    protected boolean isValidPlay() {
        return canPlay(game, player);
    }

    // Each attack may be the last: the player stops, or no target is left.
    @Override
    protected Followup resolve() {
        int viperId = galacticaBoard.pilotedFighter(player.character()).orElseThrow().id();
        return single(new ViperActivationRoundEvent(player.number(), viperId, 4, true));
    }
}
