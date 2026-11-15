package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.Comparator;
import java.util.Map;

public class SetATrapPassEventProcessor extends EventProcessor<SetATrapPassEvent> {

    // The humans choose which centurion, so the one closest to the end of the track.
    @Override
    public Followup process() {
        galacticaBoard.boardingPartyTrack().entrySet().stream()
                .max(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                .ifPresent(centurion -> ops.galactica().destroyCenturion(centurion.getKey().id()));
        return Followup.NONE;
    }
}
