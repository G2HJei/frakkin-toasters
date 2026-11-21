package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;

// All four Pegasus locations are damaged at the same time: Pegasus is destroyed. Everyone on it goes to "Sickbay" and
// its board leaves the game (rule 11.7.2.4).
public record DestroyPegasusEvent() implements Event {
}
