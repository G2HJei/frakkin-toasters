package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

// Louis Hoshi's "Organized", once per game, outside the "Brig": he takes the actions of 3 different undamaged
// locations, none of them Cylon, wherever he is (OrganizedLocationEvent).
public record OrganizedEvent(int playerNumber) implements CharacterActionEvent {
}
