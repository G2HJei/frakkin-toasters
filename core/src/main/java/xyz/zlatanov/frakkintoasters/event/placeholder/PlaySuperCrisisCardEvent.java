package xyz.zlatanov.frakkintoasters.event.placeholder;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;

public record PlaySuperCrisisCardEvent(int playerNumber, SuperCrisisCard superCrisisCard) implements PlayerEvent {
}
