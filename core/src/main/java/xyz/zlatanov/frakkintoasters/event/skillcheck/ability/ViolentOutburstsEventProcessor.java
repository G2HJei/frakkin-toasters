package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;

public class ViolentOutburstsEventProcessor extends EventProcessor<ViolentOutburstsEvent> {

    @Override
    public Followup process() {
        val currPlayer = game.currentPlayer();
        if (currPlayer.isHuman() && game.locate(currPlayer.character()).isPresent()) {
            ops.player(currPlayer.number()).sendTo(SICKBAY);
        }
        return Followup.NONE;
    }
}
