package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsOfColorEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;

public class AssignMissionSpecialistEventProcessor extends QuorumCardEventProcessor<AssignMissionSpecialistEvent> {

    public static boolean canPlay(GameView game, PlayerView player) {
        return RoleCards.anyOther(game, player);
    }

    @Override
    public QuorumCard card() {
        return QuorumCard.ASSIGN_MISSION_SPECIALIST;
    }

    @Override
    protected boolean isValidPlay() {
        return RoleCards.canHoldOther(game, player, event.holderPlayerNumber());
    }

    @Override
    protected Followup resolve() {
        ops.titles().putInPlay(card(), event.holderPlayerNumber());
        return single(new DrawSkillCardsOfColorEvent(player.number(), POLITICS, 2));
    }
}
