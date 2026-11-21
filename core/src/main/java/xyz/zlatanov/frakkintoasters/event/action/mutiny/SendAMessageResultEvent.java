package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Event;

// "Send a Message", after the die roll: the attack on the centurion, as the "Armory"'s, with 2 added to the result.
public record SendAMessageResultEvent(int centurionId) implements Event {
}
