package xyz.zlatanov.frakkintoasters.event.interrupt;

import xyz.zlatanov.frakkintoasters.event.Event;

// Reveals the cards played into the open window once everyone has answered, settles conflicts, and resolves the cards.
public record CloseInterruptWindowEvent() implements Event {
}
