package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.event.Event;

// Consult the Hybrid (Daybreak crisis)
// PASS: The current player draws a Mutiny Card and 2 Skill Cards (they may be from outside his skill set).
// Covers the 2 Skill Cards. The Mutiny Card is drawn by a separate DrawMutinyCardEvent.
public record ConsultTheHybridPassEvent() implements Event {
}
