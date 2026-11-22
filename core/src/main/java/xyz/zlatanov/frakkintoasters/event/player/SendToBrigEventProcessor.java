package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextCagEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

public class SendToBrigEventProcessor extends EventProcessor<SendToBrigEvent> {

    @Override
    protected boolean isValid() {
        return isPlayer(event.targetPlayerNumber());
    }

    // A character already in the "Brig" or a revealed Cylon can't be sent there (rule 9.2.3). A player who chooses one
    // chooses among the others (ChooseCharacterForBrigEvent).
    public static boolean canBeSentToBrig(GameView game, int playerNumber) {
        val target = game.player(playerNumber);
        return target.isHuman() && !game.boards().galactica().charactersIn(BRIG).contains(target.character());
    }

    @Override
    public Followup process() {
        // An effect queued before he got there names him all the same, e.g. a skill check's fail after a Mutiny Card
        // that one of its discards drew sent him there. A revealed Cylon ignores it (rule 6.1.2.1).
        if (!canBeSentToBrig(game, event.targetPlayerNumber())) {
            return Followup.NONE;
        }
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
