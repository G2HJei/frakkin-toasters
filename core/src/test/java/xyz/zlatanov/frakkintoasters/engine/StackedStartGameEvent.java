package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

import java.util.List;

// The first event of an engine test that plays a whole game: it starts the game like StartGameEvent does, then puts the
// Crisis Cards on top of the Crisis deck, so the turns meet the cards the test scripts. It lives in the tests, so the
// processor registry test doesn't see it.
public record StackedStartGameEvent(List<CrisisCard> crisisCards) implements Event {
}
