package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.crisis.ResolveCrisisCardEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.CAPRICA;
import static xyz.zlatanov.frakkintoasters.state.character.Character.AARON_DORAL;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.DETENTE;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.FOOD_SHORTAGE;

// Caprica: "draw 2 Crisis Cards, choose 1 to resolve, and place the other on the bottom of the deck."
class ResolveCapricaCrisisCardEventProcessorTest extends EventTestHarness<ResolveCapricaCrisisCardEvent> {

    @BeforeEach
    void setUp() {
        setUpGame(4);
        player(1).character(AARON_DORAL);
        moveTo(CAPRICA, AARON_DORAL);
        crisisDeck.nextCard(DETENTE, FOOD_SHORTAGE);
        ops.player(1).drawCrisisCard();
        ops.player(1).drawCrisisCard();
    }

    @Test
    void shouldResolveTheChosenCard() {
        execute(new ResolveCapricaCrisisCardEvent(1, FOOD_SHORTAGE, DETENTE));

        assertFollowup(new ResolveCrisisCardEvent(FOOD_SHORTAGE));
    }

    // The card counts for Prepare for Jump even on a revealed Cylon's turn (rule 6.1.3.4).
    @Test
    void shouldPlayTheCardIntoTheTurn() {
        execute(new ResolveCapricaCrisisCardEvent(1, FOOD_SHORTAGE, DETENTE));
        assertFollowup(new ResolveCrisisCardEvent(FOOD_SHORTAGE));

        assertEquals(List.of(FOOD_SHORTAGE), game.turn().crisisCards());
    }

    @Test
    void shouldPlaceTheOtherCardOnTheBottomOfTheDeck() {
        execute(new ResolveCapricaCrisisCardEvent(1, FOOD_SHORTAGE, DETENTE));
        assertFollowup(new ResolveCrisisCardEvent(FOOD_SHORTAGE));

        assertEquals(DETENTE, crisisDeck.cards().getLast());
    }

    @Test
    void shouldNotTakeNoCard() {
        assertInvalid(new ResolveCapricaCrisisCardEvent(1, null, DETENTE));
    }
}
