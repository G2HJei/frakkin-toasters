package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;

public class ViolentOutburstsEventProcessor extends EventProcessor<ViolentOutburstsEvent> {

    @Override
    public Followup process() {
        val currPlayer = game.currentPlayer();
        val location = game.locate(currPlayer.character());
        // A character in the "Brig" stays there (rule 9.3.2).
        if (currPlayer.isHuman() && location.isPresent() && !location.equals(Optional.of(BRIG))) {
            ops.player(currPlayer.number()).sendTo(SICKBAY);
        }
        return Followup.NONE;
    }
}
