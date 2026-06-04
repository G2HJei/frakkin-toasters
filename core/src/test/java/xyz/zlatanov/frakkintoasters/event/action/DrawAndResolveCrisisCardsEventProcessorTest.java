package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ResolveCapricaCrisisCardEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.AIRLOCK_LEAK;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.DETENTE;

class DrawAndResolveCrisisCardsEventProcessorTest extends EventTestHarness<DrawAndResolveCrisisCardsEvent> {

    @Test
    void shouldFollowUpWithOneOfTheCrisisCards() {
        crisisDeck.nextCard(DETENTE, AIRLOCK_LEAK);
        execute(new DrawAndResolveCrisisCardsEvent(1));
        assertFollowup(
                one(
                        new ResolveCapricaCrisisCardEvent(1, DETENTE, AIRLOCK_LEAK),
                        new ResolveCapricaCrisisCardEvent(1, AIRLOCK_LEAK, DETENTE)));
    }
}
