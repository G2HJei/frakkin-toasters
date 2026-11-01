package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Admiral's Quarters (Galactica location) - skill check difficulty: 7, colors: LEADERSHIP, TACTICS
// Action: Choose a character, then pass this skill check to send him to the "Brig."
public record AdmiralsQuartersEvent(int playerNumber, int targetPlayer) implements PlayerEvent {
}
