package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.CardsSeen;
import xyz.zlatanov.frakkintoasters.event.deck.Discard1MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.BLACKMAIL;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.PANIC;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.SELFISH_ACT;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.TOM_ZAREK_ALT;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.NECESSARY_STEPS;

class NecessaryStepsEventProcessorTest extends EventTestHarness<NecessaryStepsEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(TOM_ZAREK_ALT);
        moveTo(COMMAND, LAURA_ROSLIN);
    }

    // Zarek draws the top 2 instead of the player, and sees them.
    @Test
    void zarekPicksTheCardThePlayerGets() {
        mutinyDeck.nextCard(BLACKMAIL, PANIC);

        assertEquals(Optional.of(one(new NecessaryStepsEvent(2, 1, BLACKMAIL, PANIC, false),
                        new NecessaryStepsEvent(2, 1, PANIC, BLACKMAIL, false))),
                NecessaryStepsEventProcessor.drawFor(game, ops, 1, false));
        assertTrue(journal.facts().contains(new CardsSeen(2, DeckId.MUTINY, List.of(BLACKMAIL, PANIC))));
        assertInvalid(new NecessaryStepsEvent(2, 1, null, PANIC, false)); // a card to give
    }

    @Test
    void theCardGoesToThePlayerAndTheOtherToTheBottom() {
        execute(new NecessaryStepsEvent(2, 1, BLACKMAIL, PANIC, false));

        assertEquals(List.of(BLACKMAIL), player(1).mutinyCards().cards());
        assertEquals(PANIC, mutinyDeck.bottom());
        assertTrue(game.turn().hasUsed(2, NECESSARY_STEPS));
    }

    // The second Mutiny Card still sends him to the "Brig".
    @Test
    void theBrigRulesStillApply() {
        ops.player(1).holdMutinyCard(SELFISH_ACT);

        execute(new NecessaryStepsEvent(2, 1, BLACKMAIL, PANIC, false));

        assertFollowup(all(new SendToBrigEvent(1), new PlayerDecisionEvent<>(1, Discard1MutinyCardEvent.class)));
    }

    @Test
    void onlyZarek() {
        assertInvalid(new NecessaryStepsEvent(1, 2, BLACKMAIL, PANIC, false));
    }
}
