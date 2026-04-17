package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.fake.FakeDeck;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.DETENTE;
import static xyz.zlatanov.frakkintoasters.state.util.AllCardsProvider.genericDeck;

class HumanFleetLookAtTopCrisisCardPlayerEventTest {

    FakeDeck<CrisisCard> crisisDeck = new FakeDeck<>(genericDeck(CrisisCard.class));
    Game                 game       = Game.builder()
            .decks(DecksHolder.builder()
                    .crisis(crisisDeck)
                    .build())
            .build();

    @Test
    void shouldDrawCardAndFollowUpWithPlacementChoiceAndSkillDraw() {
        crisisDeck.nextCard(DETENTE);
        val initialSize = crisisDeck.size();

        val followups = new HumanFleetLookAtTopCrisisCardPlayerEvent(1).execute(game);

        assertEquals(initialSize - 1, crisisDeck.size());
        assertEquals(followWith(
                        one(new PlaceCrisisCardOnTopEvent(1, DETENTE),
                                new PlaceCrisisCardOnBottomEvent(1, DETENTE)),
                        single(new PlayerDecisionEvent(1, ReceiveSkillsEvent.class))
                ),
                followups);
    }
}
