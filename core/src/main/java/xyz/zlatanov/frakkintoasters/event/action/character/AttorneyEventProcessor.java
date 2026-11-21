package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.MoveFromBrigEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveFromBrigEventProcessor;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

public class AttorneyEventProcessor extends CharacterActionEventProcessor<AttorneyEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.ATTORNEY;
    }

    public static boolean canUse(GameView game, PlayerView player) {
        return !game.boards().galactica().charactersIn(BRIG).isEmpty();
    }

    @Override
    protected boolean isValidUse() {
        return isPlayer(event.targetPlayerNumber())
                && galacticaBoard.charactersIn(BRIG).contains(game.player(event.targetPlayerNumber()).character())
                && MoveFromBrigEventProcessor.isValidDestination(event.destination());
    }

    // Another player's character: Romo takes his Skill Cards too.
    @Override
    protected Followup resolve() {
        if (event.targetPlayerNumber() != event.playerNumber()) {
            val hand = game.player(event.targetPlayerNumber()).skillCards().cards();
            ops.player(event.targetPlayerNumber()).giveSkillCards(event.playerNumber(), hand);
        }
        return single(new MoveFromBrigEvent(event.targetPlayerNumber(), event.destination()));
    }
}
