package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.event.Event;

// Personal Vices - color: TREACHERY, values: 3
// Skill Check: Each human player draws 1 Treachery Card. If the current player is a human player, he also draws 1
// Mutiny Card.
public record PersonalVicesEvent() implements Event {
}
