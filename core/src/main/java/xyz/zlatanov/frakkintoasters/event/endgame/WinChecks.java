package xyz.zlatanov.frakkintoasters.event.endgame;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA;
import static xyz.zlatanov.frakkintoasters.state.track.BoardingParty.HUMANS_LOSE;

/**
 * The Cylon wins of rule 16.1 that can happen in the middle of anything. The engine asks after every event and runs the
 * result before what the event queued. The resource check waits for the end of the turn, in {@code EndTurnEvent}.
 */
@NoArgsConstructor(access = PRIVATE)
public final class WinChecks {

    private static final int DAMAGED_LOCATIONS_TO_DESTROY_GALACTICA = 6;

    public static Followup check(GameView game) {
        if (game.outcome().isPresent()) {
            return NONE;
        }
        val galactica = game.boards().galactica();
        val boarded = galactica.boardingPartyTrack().containsValue(HUMANS_LOSE);
        val damagedLocations = GALACTICA.locations().stream().filter(galactica::isDamaged).count();
        return boarded || damagedLocations >= DAMAGED_LOCATIONS_TO_DESTROY_GALACTICA
                ? single(new CylonsWinEvent())
                : NONE;
    }
}
