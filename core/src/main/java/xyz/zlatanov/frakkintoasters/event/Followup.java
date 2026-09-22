package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlayFromDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlaySkillsEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ResolveSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ShuffleAndDivideCardsEvent;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.*;

import static java.util.stream.Collectors.toCollection;

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

    static Followup all(Event... events) {
        if (events.length == 0) {
            return NONE;
        }
        if (events.length == 1) {
            return new Single(events[0]);
        }
        return new AllOf(Arrays.stream(events)
                .<Followup>map(Single::new)
                .toList());
    }

    static Followup one(Event... events) {
        if (events.length == 0) {
            return NONE;
        }
        if (events.length == 1) {
            return new Single(events[0]);
        }
        return new OneOf(Arrays.stream(events)
                .<Followup>map(Single::new)
                .collect(toCollection(LinkedHashSet::new)));
    }

    static Followup all(Followup... followups) {
        if (followups.length == 0) {
            return NONE;
        }
        if (followups.length == 1) {
            return followups[0];
        }
        return new AllOf(Arrays.stream(followups).toList());
    }

    static Followup one(Followup... followups) {
        if (followups.length == 0) {
            return NONE;
        }
        if (followups.length == 1) {
            return followups[0];
        }
        return new OneOf(Arrays.stream(followups).collect(toCollection(LinkedHashSet::new)));
    }

    static Followup skillCheckFollowup(Game game) {
        val followupEvents = new ArrayList<Event>();
        followupEvents.add(new PlayFromDestinyDeckEvent());
        addPlayerDecisions(game, followupEvents);
        followupEvents.add(new ShuffleAndDivideCardsEvent());
        followupEvents.add(new ResolveSkillCheckEvent());
        return all(followupEvents.toArray(Event[]::new));
    }

    private static void addPlayerDecisions(Game game, ArrayList<Event> followupEvents) {
        var decidingPlayer = game.currentPlayer();
        do {
            followupEvents.add(new PlayerDecisionEvent<>(decidingPlayer, PlaySkillsEvent.class));
            decidingPlayer = game.players().size() < decidingPlayer ? 1 : ++decidingPlayer;
        } while (decidingPlayer != game.currentPlayer());
    }
}
