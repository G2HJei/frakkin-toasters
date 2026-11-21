package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;

// Galactica would be damaged. While Pegasus is in play, the current player may draw a Pegasus damage token instead
// (rule 11.7.2).
public record DamageBattlestarEvent() implements Event {
}
