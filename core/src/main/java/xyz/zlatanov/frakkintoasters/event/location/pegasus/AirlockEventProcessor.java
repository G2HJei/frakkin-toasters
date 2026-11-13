package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.FollowupUtils;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.player.ExecuteEventProcessor.canBeExecuted;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.AIRLOCK;

public class AirlockEventProcessor extends LocationActionProcessor<AirlockEvent> {

    public static boolean canAct(GameView game) {
        return game.players().stream().anyMatch(p -> canBeExecuted(game, p.number()));
    }

    @Override
    protected boolean isValidAction() {
        // The pass result executes the target, so he has to be someone who can be executed.
        return isPlayer(event.targetPlayer()) && canBeExecuted(game, event.targetPlayer());
    }

    @Override
    public Followup process() {
        ops.skillCheck().start(AIRLOCK, event.targetPlayer());
        return FollowupUtils.beforeSkillCheckFollowups(game);
    }
}
