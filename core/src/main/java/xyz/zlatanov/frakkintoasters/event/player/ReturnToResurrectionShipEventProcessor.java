package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.character.CharacterMoves;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.character.MeticulousEvent;
import xyz.zlatanov.frakkintoasters.event.action.character.MeticulousEventProcessor;
import xyz.zlatanov.frakkintoasters.event.character.CharacterAbilities;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.discardDownTo;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESURRECTION_SHIP;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.METICULOUS;

public class ReturnToResurrectionShipEventProcessor extends EventProcessor<ReturnToResurrectionShipEvent> {

    static final int SKILL_CARDS_KEPT_IN_BRIG = 3;

    // "An Infiltrating Cylon Leader may always return", also from the "Brig". Leoben Conoy's "Clouded" still forbids it
    // while another character is there: the return is a move he chooses.
    public static boolean canReturn(GameView game, PlayerView player) {
        return player.isInfiltrating() && game.locate(player.character()).isPresent()
                && !CharacterMoves.cloudedBlocks(game, player, RESURRECTION_SHIP);
    }

    /**
     * "If you were in the "Brig", you must then discard down to 3 cards."
     */
    public static Followup endInfiltration(GameView game, GameOps ops, PlayerView player, Location destination) {
        val inBrig = game.locate(player.character()).filter(BRIG::equals).isPresent();
        ops.player(player.number()).endInfiltration(destination);
        return inBrig && player.skillCards().size() > SKILL_CARDS_KEPT_IN_BRIG
                ? single(new PlayerDecisionEvent<>(player.number(), DiscardSkillCardsEvent.class,
                discardDownTo(SKILL_CARDS_KEPT_IN_BRIG)))
                : NONE;
    }

    @Override
    protected boolean isValid() {
        return canReturn(game, player);
    }

    // Aaron Doral may go to another Cylon location instead, with "Meticulous".
    @Override
    public Followup process() {
        if (CharacterAbilities.canUseMiracle(game, player, METICULOUS)) {
            return one(Stream.concat(Stream.of(RESURRECTION_SHIP), MeticulousEventProcessor.destinations().stream())
                    .map(location -> new MeticulousEvent(player.number(), location))
                    .toArray(Event[]::new));
        }
        return endInfiltration(game, ops, player, RESURRECTION_SHIP);
    }
}
