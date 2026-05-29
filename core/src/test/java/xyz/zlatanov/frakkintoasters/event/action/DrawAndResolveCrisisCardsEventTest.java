package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTest;
import xyz.zlatanov.frakkintoasters.event.ResolveCapricaCrisisCardEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.AIRLOCK_LEAK;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.DETENTE;

class DrawAndResolveCrisisCardsEventTest extends EventTest {

    @Test
    void shouldFollowUpWithOneOfTheCrisisCards() {
        crisisDeck.nextCard(DETENTE).nextCard(AIRLOCK_LEAK);
        val followup = new DrawAndResolveCrisisCardsEvent(1).execute(game);
        assertEquals(one(
                        new ResolveCapricaCrisisCardEvent(1, DETENTE, AIRLOCK_LEAK),
                        new ResolveCapricaCrisisCardEvent(1, AIRLOCK_LEAK, DETENTE)
                ),
                followup);
    }
}
