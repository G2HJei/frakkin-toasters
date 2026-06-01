package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;

public record PlaceDestinationCardAtBottomEvent(int playerNumber, DestinationCard card) implements PlayerEvent {

}
