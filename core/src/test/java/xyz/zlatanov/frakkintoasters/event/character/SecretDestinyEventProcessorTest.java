package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.crisis.ResolveCrisisCardEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.SECRET_DESTINY;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.DETENTE;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.RAIDERS_INBOUND;

class SecretDestinyEventProcessorTest extends EventTestHarness<SecretDestinyEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        ops.playCrisisCard(RAIDERS_INBOUND);
    }

    // The revealed card is discarded and leaves the turn, the new one takes its place and resolves.
    @Test
    void replacesTheRevealedCard() {
        crisisDeck.nextCard(DETENTE);

        execute(new SecretDestinyEvent(1, RAIDERS_INBOUND));

        assertEquals(List.of(DETENTE), game.turn().crisisCards());
        assertEquals(List.of(RAIDERS_INBOUND), crisisDeck.discardedCards());
        assertEquals(0, player(1).miracleTokens());
        assertTrue(game.turn().hasUsed(1, SECRET_DESTINY));
        assertFollowup(new ResolveCrisisCardEvent(DETENTE));
    }

    @Test
    void onlyTheCardJustRevealed() {
        assertInvalid(new SecretDestinyEvent(1, DETENTE));
    }

    @Test
    void onlyWithAMiracleToken() {
        ops.player(1).loseMiracleTokens();

        assertInvalid(new SecretDestinyEvent(1, RAIDERS_INBOUND));
    }

    @Test
    void onlyWhenACardIsLeftToDraw() {
        crisisDeck.clear();

        assertInvalid(new SecretDestinyEvent(1, RAIDERS_INBOUND));
    }
}
