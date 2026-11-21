package xyz.zlatanov.frakkintoasters.event.officials;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.ArrayList;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

@NoArgsConstructor(access = PRIVATE)
public final class BrigTitles {

    /**
     * @return after a character left the "Brig": while the Admiral or the CAG is still in it, every human was, so the
     * title goes to the one who left now (rules 4.4.2.3 and 4.4.3.2). The line of succession, which prefers characters
     * outside the "Brig", picks him
     */
    public static Followup afterLeaving(GameView game) {
        val brig = game.boards().galactica().charactersIn(BRIG);
        val followups = new ArrayList<Event>();
        if (game.admiral() != null && brig.contains(game.admiral())) {
            followups.add(new DetermineNextAdmiralEvent());
        }
        if (game.cag() != null && brig.contains(game.cag())) {
            followups.add(new DetermineNextCagEvent());
        }
        return all(followups.toArray(Event[]::new));
    }
}
