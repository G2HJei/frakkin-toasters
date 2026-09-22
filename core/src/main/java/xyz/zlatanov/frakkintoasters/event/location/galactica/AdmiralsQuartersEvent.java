package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record AdmiralsQuartersEvent(int playerNumber, int targetPlayer) implements PlayerEvent {
}
