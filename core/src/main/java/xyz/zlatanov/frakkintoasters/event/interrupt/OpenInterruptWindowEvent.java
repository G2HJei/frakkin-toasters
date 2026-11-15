package xyz.zlatanov.frakkintoasters.event.interrupt;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow;

// Asks the players who hold a card for the window whether they play it. See "Interrupt Cards" in CLAUDE.md.
public record OpenInterruptWindowEvent(InterruptWindow window) implements Event {
}
