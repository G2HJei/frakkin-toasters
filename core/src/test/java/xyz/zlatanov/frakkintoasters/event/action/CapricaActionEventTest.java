package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.placeholder.DrawAndResolveCrisisCardsEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlaySuperCrisisCardEvent;
import xyz.zlatanov.frakkintoasters.state.Game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.followWith;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;

class CapricaActionEventTest {

	Game game = Game.builder().build();

	@Test
	void shouldOfferChoiceBetweenSuperCrisisAndCrisisCards() {
		val followups = new CapricaActionEvent(1).execute(game);

		val expectedFollowups = followWith(one(
				new PlaySuperCrisisCardEvent(1),
				new DrawAndResolveCrisisCardsEvent(1)));
		assertEquals(expectedFollowups, followups);
	}
}
