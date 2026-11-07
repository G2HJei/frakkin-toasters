package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARL_HELO_AGATHON;

public class ViolentOutburstsEventProcessor extends EventProcessor<ViolentOutburstsEvent> {

    @Override
    public Followup process() {
        val currPlayer = game.currentPlayer();
        if (currPlayer.isHuman() && (currPlayer.character() != KARL_HELO_AGATHON || game.turn() > 1)) {
            ops.player(currPlayer.number()).sendTo(SICKBAY);
        }
        return Followup.NONE;
    }
}
