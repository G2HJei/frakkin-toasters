package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextCagEvent;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

public class SendToBrigEventProcessor extends EventProcessor<SendToBrigEvent> {

    // A character already in the "Brig" or a revealed Cylon can't be sent there (rule 9.2.3).
    @Override
    protected boolean isValid() {
        val target = game.player(event.targetPlayerNumber());
        return target.isHuman() && !galacticaBoard.charactersIn(BRIG).contains(target.character());
    }


    @Override
    public Followup process() {
        val character = game.player(event.targetPlayerNumber()).character();
        if (game.locate(character).isEmpty()) {
            // A character who isn't on the board can't be moved, e.g. a "Stranded" Karl "Helo" Agathon.
            return Followup.NONE;
        }
        ops.player(event.targetPlayerNumber()).sendTo(BRIG);
        val followups = new ArrayList<Event>();
        // The Admiral and the CAG lose their titles in the "Brig", the President keeps hers (rule 4.4).
        if (character == game.admiral()) {
            followups.add(new DetermineNextAdmiralEvent());
        }
        if (character == game.cag()) {
            followups.add(new DetermineNextCagEvent());
        }
        return Followup.all(followups.toArray(Event[]::new));
    }
}
