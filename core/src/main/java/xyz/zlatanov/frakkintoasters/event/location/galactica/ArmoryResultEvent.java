package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.Event;

// The attack of the "Armory" on a centurion, after the die roll: destroyed on 7-8.
public record ArmoryResultEvent(int centurionId) implements Event {
}
