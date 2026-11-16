package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.event.Event;

// What the roll of a risk decides: the reward, or the risked ships are damaged (vipers) or destroyed (raptors).
public record RiskResultEvent(RiskShipsEvent.Risk risk) implements Event {
}
