package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEventProcessor;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_APOLLO_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

// "To play a seven player game, one player must play a Cylon Leader" (rule A3.7.2.2). The first six seats pick humans,
// and the last one has to be able to pick the Leader: the setup goes on, with restore giving the same game at every
// pause.
class SevenPlayerSetupTest {

    private static final int PLAYERS = 7;

    private GameEngine engine;

    @Test
    void theLastSeatPicksTheCylonLeader() {
        engine = GameEngine.start(PLAYERS, 7L);
        assertRestoresTheSameGame();

        while (characters().size() < PLAYERS) {
            switch (engine.pendingInput().orElseThrow()) {
                case AwaitingDecision(var decision) when decision.action() == SelectCharacterEvent.class -> {
                    val player = decision.playerNumber();
                    engine.submit(player, new SelectCharacterEvent(player, pick()));
                }
                // a character with two start locations, e.g. D'Anna Biers
                case AwaitingChoice choice -> engine.choose(choice.player(), choice.options().iterator().next());
                default -> fail("Waiting for " + engine.pendingInput() + " during the character selection");
            }
            assertRestoresTheSameGame();
        }

        assertEquals(1, characters().stream().filter(character -> character.type() == CYLON_LEADER).count());
        assertEquals(PLAYERS, characters().stream().distinct().count());
    }

    // A human whenever the rules allow one, otherwise a Cylon Leader. Lee "Apollo" Adama would ask for his viper.
    private Character pick() {
        val legal = Arrays.stream(Character.values())
                .filter(character -> character != LEE_APOLLO_ADAMA)
                .filter(character -> SelectCharacterEventProcessor.canBeSelected(engine.view(), character))
                .toList();
        return legal.stream()
                .filter(character -> character.type() != CYLON_LEADER)
                .findFirst()
                .orElseGet(() -> legal.stream().findFirst().orElseThrow(() -> new AssertionError("No legal character")));
    }

    private List<Character> characters() {
        return engine.view().players().stream().map(PlayerView::character).filter(Objects::nonNull).toList();
    }

    private void assertRestoresTheSameGame() {
        val restored = GameEngine.restore(engine.journal());

        assertEquals(GameSnapshot.of((Game) engine.view()), GameSnapshot.of((Game) restored.view()));
        assertEquals(statuses(engine), statuses(restored));
    }

    private static List<EngineStatus> statuses(GameEngine engine) {
        return IntStream.rangeClosed(1, PLAYERS).mapToObj(engine::status).toList();
    }
}
