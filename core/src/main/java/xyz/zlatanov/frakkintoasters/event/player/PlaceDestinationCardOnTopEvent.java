package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;

import java.util.List;

public record PlaceDestinationCardOnTopEvent(int playerNumber, DestinationCard card) implements PlayerEvent {

	@Override
	public List<Followup> apply(Game game) {
		game.decks().destination().add(card);
		return List.of();
	}
}
