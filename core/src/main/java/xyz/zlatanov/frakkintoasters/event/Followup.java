package xyz.zlatanov.frakkintoasters.event;

import java.util.Arrays;
import java.util.List;

public sealed interface Followup permits Followup.Single, Followup.AllOf, Followup.OneOf {

    record Single(Event event) implements Followup {
    }

    record AllOf(List<Followup> followups) implements Followup {
    }

    record OneOf(List<Followup> options) implements Followup {
    }

    static Single single(Event event) {
        return new Single(event);
    }

    static AllOf all(Event... events) {
        return new AllOf(Arrays.stream(events).<Followup>map(Single::new).toList());
    }

    static OneOf one(Event... events) {
        return new OneOf(Arrays.stream(events).<Followup>map(Single::new).toList());
    }

    static AllOf all(Followup... followups) {
        return new AllOf(Arrays.stream(followups).toList());
    }

    static OneOf one(Followup... followups) {
        return new OneOf(Arrays.stream(followups).toList());
    }

    static List<Followup> followWith(Event event) {
        return followWith(single(event));
    }

    static List<Followup> followWith(Followup... followups) {
        return Arrays.stream(followups).toList();
    }
}
