package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

import java.util.List;

public record PlaceCrisisCardOnTopEvent(int playerNumber, CrisisCard card) implements PlayerEvent {

	@Override
	public List<Followup> apply(Game game) {
		game.decks().crisis().add(card);
		return List.of();
	}
}
