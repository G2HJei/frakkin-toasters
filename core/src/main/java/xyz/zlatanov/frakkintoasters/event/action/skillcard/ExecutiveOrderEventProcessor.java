package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.Actions;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;
import xyz.zlatanov.frakkintoasters.event.action.ResetActingPlayerEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEventProcessor;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.ACTION;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.MOVE;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.EXECUTIVE_ORDER;
import static xyz.zlatanov.frakkintoasters.event.action.skillcard.CriticalSituationEventProcessor.criticalSituationOrExecutiveOrderPlayed;

public class ExecutiveOrderEventProcessor extends SkillCardEventProcessor<ExecutiveOrderEvent> {

    public static boolean canPlay(GameView game, PlayerView player) {
        return !criticalSituationOrExecutiveOrderPlayed(game)
                && game.players().stream().anyMatch(target -> canTarget(game, player, target));
    }

    // Another player, who can move or act. A Cylon player may not benefit from it, and a revealed one can't be targeted
    // (A2.3.8.4, A2.3.8.6).
    private static boolean canTarget(GameView game, PlayerView player, PlayerView target) {
        return target.number() != player.number()
                && target.isHuman()
                && (Actions.canMove(game, target) || Actions.hasAnAction(game, target));
    }

    @Override
    public SkillCardType type() {
        return SkillCardType.EXECUTIVE_ORDER;
    }

    @Override
    protected boolean isValidPlay() {
        return isPlayer(event.targetPlayerNumber())
                && !criticalSituationOrExecutiveOrderPlayed(game)
                && canTarget(game, player, game.player(event.targetPlayerNumber()));
    }

    // The target chooses: move and then act, or act twice without moving. Moving is only offered when he can move,
    // e.g. not from the "Brig".
    @Override
    protected Followup resolve() {
        int target = event.targetPlayerNumber();
        val twoActions = all(new GrantEvent(target, ACTION, EXECUTIVE_ORDER), new GrantEvent(target, ACTION, EXECUTIVE_ORDER));
        val choice = Actions.canMove(game, game.player(target))
                ? one(all(new GrantEvent(target, MOVE, EXECUTIVE_ORDER), new GrantEvent(target, ACTION, EXECUTIVE_ORDER)),
                twoActions)
                : twoActions;
        return all(choice, single(new ResetActingPlayerEvent()));
    }
}
