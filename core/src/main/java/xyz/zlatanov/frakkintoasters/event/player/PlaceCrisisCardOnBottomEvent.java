package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

public record PlaceCrisisCardOnBottomEvent(int playerNumber, CrisisCard card) implements PlayerEvent {

	@Override
	public Followup apply(Game game) {
		game.decks().crisis().addToBottom(card);
		return Followup.NONE;
	}
}
