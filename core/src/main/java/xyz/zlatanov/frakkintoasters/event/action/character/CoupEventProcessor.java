package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.event.officials.BrigTitles;
import xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEventProcessor;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;

public class CoupEventProcessor extends CharacterActionEventProcessor<CoupEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.COUP;
    }

    // Out of the "Brig" he has to be able to hold the title; from the "Brig" he moves out first.
    public static boolean canUse(GameView game, PlayerView player) {
        return inBrig(game, player)
                ? player.isHuman() && player.character() != game.admiral()
                : MakeAdmiralEventProcessor.canBecomeAdmiral(game, player.character());
    }

    @Override
    protected boolean isValidUse() {
        return canUse(game, player);
    }

    @Override
    protected Followup resolve() {
        val fromBrig = inBrig(game, player);
        if (fromBrig) {
            ops.player(event.playerNumber()).moveTo(Location.COMMAND);
        }
        ops.titles().assignAdmiral(player.character());
        // Leaving the "Brig" may hand him the CAG title too, when everyone else is there.
        return fromBrig ? BrigTitles.afterLeaving(game) : NONE;
    }

    private static boolean inBrig(GameView game, PlayerView player) {
        return game.locate(player.character()).filter(Location.BRIG::equals).isPresent();
    }
}
