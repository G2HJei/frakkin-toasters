package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.placeholder.ResolveCapricaCrisisCardEvent;
import xyz.zlatanov.frakkintoasters.fake.FakeDeck;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.followWith;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.AIRLOCK_LEAK;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.DETENTE;
import static xyz.zlatanov.frakkintoasters.state.util.AllCardsProvider.genericDeck;

class DrawAndResolveCrisisCardsEventTest {
    FakeDeck<CrisisCard> crisisCardFakeDeck = new FakeDeck<>(genericDeck(CrisisCard.class));
    Game                 game               =
            Game.builder()
                    .decks(DecksHolder.builder()
                            .crisis(crisisCardFakeDeck)
                            .build())
                    .build();

    @Test
    void shouldFollowUpWithOneOfTheCrisisCards() {
        crisisCardFakeDeck.nextCard(DETENTE).nextCard(AIRLOCK_LEAK);
        val followup = new DrawAndResolveCrisisCardsEvent(1).execute(game);
        assertEquals(followWith(one(
                                new ResolveCapricaCrisisCardEvent(1, DETENTE, AIRLOCK_LEAK),
                                new ResolveCapricaCrisisCardEvent(1, AIRLOCK_LEAK, DETENTE)
                        )
                ),
                followup);
    }
}