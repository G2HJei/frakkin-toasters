package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.Event;

// Just before a skill check ends, William Adama may use "Command Authority", if he still can after its result. Queued
// only in games where someone has the ability.
public record OfferCommandAuthorityEvent() implements Event {
}
