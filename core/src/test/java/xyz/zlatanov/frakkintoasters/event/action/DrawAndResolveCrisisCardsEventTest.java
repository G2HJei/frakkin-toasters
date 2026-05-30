package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTest;
import xyz.zlatanov.frakkintoasters.event.ResolveCapricaCrisisCardEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.AIRLOCK_LEAK;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.DETENTE;

class DrawAndResolveCrisisCardsEventTest extends EventTest {

    @Test
    void shouldFollowUpWithOneOfTheCrisisCards() {
        nextCard(crisisDeck, DETENTE, AIRLOCK_LEAK);
        executeAndAssertFollowup(new DrawAndResolveCrisisCardsEvent(1),
                one(
                        new ResolveCapricaCrisisCardEvent(1, DETENTE, AIRLOCK_LEAK),
                        new ResolveCapricaCrisisCardEvent(1, AIRLOCK_LEAK, DETENTE)));
    }
}
