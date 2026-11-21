package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.action.ViperActivationRoundEvent;
import xyz.zlatanov.frakkintoasters.state.board.LocationsArea;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.FULL_THROTTLE;

public class FullThrottleEventProcessor extends SkillCardEventProcessor<FullThrottleEvent> {

    public static boolean canPlay(GameView game, PlayerView player) {
        return game.boards().galactica().pilotedFighter(player.character()).isPresent();
    }

    @Override
    public SkillCardType type() {
        return FULL_THROTTLE;
    }

    // Any space area, however far: the card lifts the limit of 1 (2 for a Mark VII).
    @Override
    protected boolean isValidPlay() {
        if (!canPlay(game, player)) {
            return false;
        }
        val destination = event.destination();
        return destination == null
                || LocationsArea.GALACTICA_SPACE.locations().contains(destination)
                && destination != game.locate(player.character()).orElseThrow();
    }

    @Override
    protected Followup resolve() {
        int viperId = galacticaBoard.pilotedFighter(player.character()).orElseThrow().id();
        if (event.destination() != null) {
            ops.ship(viperId).moveTo(event.destination());
        }
        return single(new ViperActivationRoundEvent(player.number(), viperId, 1, true));
    }
}
