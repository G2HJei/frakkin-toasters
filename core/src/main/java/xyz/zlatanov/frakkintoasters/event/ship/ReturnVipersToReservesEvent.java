package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;

// Return all vipers in space areas to the "Reserves". The characters piloting them are placed on the "Hangar Deck".
//
// @param exceptAssaultRaptors the assault raptors stay where they are, because their players decide (rule 12.1.1): the
//                             fleet jumped
public record ReturnVipersToReservesEvent(boolean exceptAssaultRaptors) implements Event {

    public ReturnVipersToReservesEvent() {
        this(false);
    }
}
