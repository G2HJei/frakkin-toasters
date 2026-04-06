package xyz.zlatanov.frakkintoasters.event;

import java.util.Arrays;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.FollowupType.ALL_OF;
import static xyz.zlatanov.frakkintoasters.event.Followup.FollowupType.ONE_OF;

public record Followup(FollowupType type, List<Event> events) {

    public static List<Followup> followWith(Event event) {
        return followWith(allOf(event));
    }

    public static List<Followup> followWith(Followup... followups) {
        return Arrays.stream(followups).toList();
    }

    public static Followup allOf(Event... events) {
        return new Followup(ALL_OF, Arrays.stream(events).toList());
    }

    public static Followup oneOf(Event... events) {
        return new Followup(ONE_OF, Arrays.stream(events).toList());
    }

    public enum FollowupType {
        ONE_OF,
        ALL_OF
    }
}
