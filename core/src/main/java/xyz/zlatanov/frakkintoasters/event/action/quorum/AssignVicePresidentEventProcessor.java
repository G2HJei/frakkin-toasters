package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsOfColorEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;

public class AssignVicePresidentEventProcessor extends QuorumCardEventProcessor<AssignVicePresidentEvent> {

    // An Infiltrating Cylon Leader can't be Vice President (rule 6.2.3.3).
    public static boolean canPlay(GameView game, PlayerView player) {
        return game.players().stream().anyMatch(other -> RoleCards.canHoldOther(game, player, other.number())
                && !other.isInfiltrating());
    }

    @Override
    public QuorumCard card() {
        return QuorumCard.ASSIGN_VICE_PRESIDENT;
    }

    @Override
    protected boolean isValidPlay() {
        return RoleCards.canHoldOther(game, player, event.holderPlayerNumber())
                && !game.player(event.holderPlayerNumber()).isInfiltrating();
    }

    @Override
    protected Followup resolve() {
        ops.titles().putInPlay(card(), event.holderPlayerNumber());
        return single(new DrawSkillCardsOfColorEvent(player.number(), POLITICS, 2));
    }
}
