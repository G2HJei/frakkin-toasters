package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

public record EngineRoomActionEvent(int playerNumber, SkillCard discardCard1,
									SkillCard discardCard2) implements ActionEvent {

	@Override
	public boolean isValid(Game game) {
		val hand = player(game).skillCards().cards();
		return hand.contains(discardCard1) && hand.contains(discardCard2)
				&& !discardCard1.equals(discardCard2);
	}

	@Override
	public Followup apply(Game game) {
		val player = player(game);
		player.skillCards().remove(List.of(discardCard1, discardCard2));
		game.decks().discard(discardCard1);
		game.decks().discard(discardCard2);
		game.boards().galactica().engineRoomActivated(true);
		return Followup.NONE;
	}
}
