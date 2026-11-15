package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;

// Return all vipers in space areas to the "Reserves". The characters piloting them are placed on the "Hangar Deck".
public record ReturnVipersToReservesEvent() implements Event {
}
