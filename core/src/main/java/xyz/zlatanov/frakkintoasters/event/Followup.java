package xyz.zlatanov.frakkintoasters.event;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public sealed interface Followup permits Followup.None, Followup.Single, Followup.AllOf, Followup.OneOf {

    Followup NONE = new None();

    record None() implements Followup {
    }

    record Single(Event event) implements Followup {
    }

    record AllOf(List<Followup> followups) implements Followup {
    }

    record OneOf(Set<Followup> options) implements Followup {
    }

    static Single single(Event event) {
        return new Single(event);
    }

    static AllOf all(Event... events) {
        return new AllOf(Arrays.stream(events).<Followup>map(Single::new).toList());
    }

    static OneOf one(Event... events) {
        return new OneOf(Arrays.stream(events).<Followup>map(Single::new).collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    static AllOf all(Followup... followups) {
        return new AllOf(Arrays.stream(followups).toList());
    }

    static OneOf one(Followup... followups) {
        return new OneOf(Arrays.stream(followups).collect(Collectors.toCollection(LinkedHashSet::new)));
    }
}
