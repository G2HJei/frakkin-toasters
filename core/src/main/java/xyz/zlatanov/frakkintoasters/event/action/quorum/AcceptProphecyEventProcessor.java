package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.Arrays;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_1_OF_ANY_COLOR;

public class AcceptProphecyEventProcessor extends QuorumCardEventProcessor<AcceptProphecyEvent> {

    // The draw needs a card left (rule 8.2.2.5).
    public static boolean canPlay(GameView game) {
        return Arrays.stream(SkillCardColor.values()).anyMatch(color -> game.decks().skillCardsLeft(color) > 0);
    }

    @Override
    public QuorumCard card() {
        return QuorumCard.ACCEPT_PROPHECY;
    }

    @Override
    protected boolean isValidPlay() {
        return canPlay(game);
    }

    // In play without a holder: it stays when the presidency changes.
    @Override
    protected Followup resolve() {
        ops.titles().putInPlay(card(), null);
        return single(new PlayerDecisionEvent<>(player.number(), DrawSkillCardsEvent.class, DRAW_EXACTLY_1_OF_ANY_COLOR));
    }
}
