package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.event.Event;

// Footage Transmitted (super crisis)
// FAIL: Each revealed Cylon player draws 2 Treachery Cards and 1 Super Crisis Card.
// Covers the Super Crisis Cards. The Treachery Cards are drawn by separate DrawSkillCardsOfColorEvents.
public record FootageTransmittedFailEvent() implements Event {
}
