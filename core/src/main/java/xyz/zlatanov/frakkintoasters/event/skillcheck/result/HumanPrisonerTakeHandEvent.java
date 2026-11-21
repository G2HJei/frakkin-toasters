package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// The Cylon who played "Human Prisoner" takes the Skill Cards of the human he picked, who then goes to "Sickbay".
public record HumanPrisonerTakeHandEvent(int playerNumber, int targetPlayerNumber) implements PlayerEvent {
}
