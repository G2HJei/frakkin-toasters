package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.event.Event;

public record ActivateRaiderEvent(int raiderShipId) implements Event {
}
