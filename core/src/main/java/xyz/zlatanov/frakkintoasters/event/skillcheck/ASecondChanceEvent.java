package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.event.Event;

// A Second Chance - color: TACTICS, values: 5
// Play before cards are added to a skill check. If the check passes the printed difficulty by 4 or more, the current
// player gains 1 miracle token.
public record ASecondChanceEvent() implements Event {
}
