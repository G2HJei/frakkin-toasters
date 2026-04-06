package xyz.zlatanov.frakkintoasters.event;

import java.util.Arrays;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.EventFollowup.FollowupType.OPTIONS;
import static xyz.zlatanov.frakkintoasters.event.EventFollowup.FollowupType.SEQUENCE;

public record EventFollowup(FollowupType type, List<Event> events) {

    public static EventFollowup followup(Event... events) {
        return new EventFollowup(SEQUENCE, Arrays.stream(events).toList());
    }

    public static EventFollowup followupOptions(Event... events) {
        return new EventFollowup(OPTIONS, Arrays.stream(events).toList());
    }

    public enum FollowupType {
        OPTIONS,
        SEQUENCE
    }
}
