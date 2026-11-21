package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// "Can send a character to Sickbay": a character on Galactica discards 5 Skill Cards and goes to "Sickbay".
public record CylonPowerSendToSickbayEvent(int playerNumber, int targetPlayerNumber) implements PlayerEvent {
}
