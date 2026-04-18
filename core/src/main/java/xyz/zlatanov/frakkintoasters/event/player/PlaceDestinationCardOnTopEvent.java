package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;

public record PlaceDestinationCardOnTopEvent(int playerNumber, DestinationCard card) implements PlayerEvent {

	@Override
	public Followup apply(Game game) {
		game.decks().destination().add(card);
		return Followup.NONE;
	}
}
