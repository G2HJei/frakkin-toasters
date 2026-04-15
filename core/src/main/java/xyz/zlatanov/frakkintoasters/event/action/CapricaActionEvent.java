package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.DrawAndResolveCrisisCardsEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlaySuperCrisisCardEvent;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.followWith;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public record CapricaActionEvent(int playerNumber) implements ActionEvent {

	@Override
	public List<Followup> apply(Game game) {
		return followWith(one(
				new PlaySuperCrisisCardEvent(playerNumber),
				new DrawAndResolveCrisisCardsEvent(playerNumber)));
	}
}
