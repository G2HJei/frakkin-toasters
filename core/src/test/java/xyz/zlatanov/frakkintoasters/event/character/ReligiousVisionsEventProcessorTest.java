package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.crisis.ResolveCrisisCardEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.PRESIDENTS_OFFICE;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESEARCH_LAB;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.RELIGIOUS_VISIONS;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.DETENTE;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.RAIDERS_INBOUND;

class ReligiousVisionsEventProcessorTest extends EventTestHarness<ReligiousVisionsEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
    }

    // The card she keeps stays with the turn and resolves, the other goes to the bottom of the deck.
    @Test
    void keepsOneCardAndPutsTheOtherAtTheBottom() {
        execute(new ReligiousVisionsEvent(1, DETENTE, RAIDERS_INBOUND));

        assertEquals(List.of(DETENTE), game.turn().crisisCards());
        assertEquals(RAIDERS_INBOUND, crisisDeck.bottom());
        assertTrue(game.turn().hasUsed(1, RELIGIOUS_VISIONS));
        assertFollowup(new ResolveCrisisCardEvent(DETENTE));
    }

    // The card she keeps is revealed, so "Secret Destiny" may replace it.
    @Test
    void theCardSheKeepsIsRevealed() {
        player(2).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);

        execute(new ReligiousVisionsEvent(1, DETENTE, RAIDERS_INBOUND));

        assertFollowup(one(new SecretDestinyEvent(2, DETENTE), new ResolveCrisisCardEvent(DETENTE)));
    }

    @Test
    void notWithoutTheAbility() {
        player(2).character(GAIUS_BALTAR);
        moveTo(RESEARCH_LAB, GAIUS_BALTAR);

        assertInvalid(new ReligiousVisionsEvent(2, DETENTE, RAIDERS_INBOUND));
    }

    @Test
    void notWithoutTheCards() {
        assertInvalid(new ReligiousVisionsEvent(1, null, RAIDERS_INBOUND));
    }

    @Test
    void drawsTwoOnlyWhenTwoAreLeft() {
        assertTrue(ReligiousVisionsEventProcessor.drawsTwo(game, player(1)));
        crisisDeck.clear();
        crisisDeck.nextCard(DETENTE);
        assertEquals(false, ReligiousVisionsEventProcessor.drawsTwo(game, player(1)));
        assertEquals(false, ReligiousVisionsEventProcessor.drawsTwo(game, player(2)));
        assertInvalid(new ReligiousVisionsEvent(2, DETENTE, RAIDERS_INBOUND));
    }
}
