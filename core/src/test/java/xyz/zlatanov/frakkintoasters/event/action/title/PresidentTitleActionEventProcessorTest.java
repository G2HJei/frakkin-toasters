package xyz.zlatanov.frakkintoasters.event.action.title;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.EULOGY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class PresidentTitleActionEventProcessorTest extends EventTestHarness<PresidentTitleActionEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        president(LAURA_ROSLIN);
    }

    @Test
    void thePresidentDrawsAQuorumCard() {
        quorumDeck.nextCard(EULOGY);

        execute(new PresidentTitleActionEvent(1));

        assertEquals(List.of(EULOGY), presidentHand.cards());
    }

    // The President keeps the title's abilities in the "Brig" (rule 4.4.1.2).
    @Test
    void alsoFromTheBrig() {
        moveTo(BRIG, LAURA_ROSLIN);

        execute(new PresidentTitleActionEvent(1));

        assertEquals(1, presidentHand.size());
    }

    @Test
    void onlyThePresident() {
        assertInvalid(new PresidentTitleActionEvent(2));
    }

    @Test
    void notWithoutACardToDraw() {
        quorumDeck.clear();

        assertInvalid(new PresidentTitleActionEvent(1));
    }
}
