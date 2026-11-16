package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.event.Event;

// "Derelict Basestar": "Lose 1 fuel. Then place 2 civilian ships behind Galactica and 1 basestar in front of Galactica.
// Damage the basestar once." This is the placement and the damage, the fuel is lost before.
public record DerelictBasestarEvent() implements Event {
}
