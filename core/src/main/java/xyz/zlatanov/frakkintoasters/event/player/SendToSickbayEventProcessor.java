package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;

public class SendToSickbayEventProcessor extends EventProcessor<SendToSickbayEvent> {

    @Override
    protected boolean isValid() {
        return isPlayer(event.targetPlayerNumber());
    }

    @Override
    public Followup process() {
        val location = game.locate(game.player(event.targetPlayerNumber()).character());
        // A character who isn't on the board can't be moved, e.g. a "Stranded" Karl "Helo" Agathon, and one in the
        // "Brig" stays there (rule 9.3.2).
        if (location.isPresent() && !location.equals(Optional.of(BRIG))) {
            ops.player(event.targetPlayerNumber()).sendTo(SICKBAY);
        }
        return Followup.NONE;
    }

    // A player choosing a character to send to "Sickbay" can't choose one who is already there, a revealed Cylon or a
    // character off the board (rule 9.3.2).
    public static boolean canBeChosenForSickbay(GameView game, int playerNumber) {
        val target = game.player(playerNumber);
        val location = game.locate(target.character());
        return target.isHuman() && location.isPresent() && !location.equals(Optional.of(SICKBAY));
    }
}
