package xyz.zlatanov.frakkintoasters.action;

import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.MUTINEER;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class CreateLoyaltyDeckActionTest {


    private static Stream<Arguments> simpleLoyaltyDeckParams() {
        val threePlayerGame = new Game(KOBOL, 3);
        val fourPlayerGame = new Game(KOBOL, 4);
        val fourPlayerGameWithCylonLeader = new Game(KOBOL, 4);
        val fivePlayerGame = new Game(KOBOL, 5);
        val fivePlayerGameWithCylonLeader = new Game(KOBOL, 5);
        val sixPlayerGame = new Game(KOBOL, 6);
        val sixPlayerGameWithCylonLeader = new Game(KOBOL, 6);
        val sevenPlayerGame = new Game(KOBOL, 7);
        return Stream.of(
                arguments(threePlayerGame, false, 1, 6, false),
                arguments(fourPlayerGame, false, 1, 8, true),
                arguments(fourPlayerGameWithCylonLeader, true, 1, 6, false),
                arguments(fivePlayerGame, false, 2, 9, false),
                arguments(fivePlayerGameWithCylonLeader, true, 1, 8, true),
                arguments(sixPlayerGame, false, 2, 11, true),
                arguments(sixPlayerGameWithCylonLeader, true, 2, 9, false),
                arguments(sevenPlayerGame, true, 2, 11, true)
        );
    }

    @ParameterizedTest
    @MethodSource("simpleLoyaltyDeckParams")
    void shouldCreateSimpleLoyaltyDeck(Game game, boolean pickCylonLeader, int youAreACylonCount, int notACylonCount, boolean hasMutineer) {
        pickCharacters(game, pickCylonLeader);
        new CreateLoyaltyDeckAction().execute(game);
        val loyaltyCards = game.decks().loyalty().cards();
        assertDeckComposition(notACylonCount, youAreACylonCount, hasMutineer, loyaltyCards);
    }

    private static void pickCharacters(Game game, boolean pickCylonLeader) {
        game.player(1).selectCharacter(KARA_STARBUCK_THRACE);
        game.player(2).selectCharacter(WILLIAM_ADAMA);
        game.player(3).selectCharacter(LAURA_ROSLIN);
        val playerCount = game.players().size();
        if (playerCount > 3) {
            game.player(4).selectCharacter(pickCylonLeader ? CAVIL : CHIEF_GALEN_TYROL);
        }
        if (playerCount > 4) {
            game.player(5).selectCharacter(CALLANDRA_CALLY_TYROL);
        }
        if (playerCount > 5) {
            game.player(6).selectCharacter(ANASTASIA_DEE_DUALLA);
        }
        if (playerCount == 7) {
            game.player(7).selectCharacter(SHERMAN_DOC_COTTLE);
        }
    }

    private static void assertDeckComposition(int notACylonCount, int youAreACylonCount, boolean mutineer, List<LoyaltyCard> loyaltyCards) {
        assertEquals(mutineer, loyaltyCards.contains(MUTINEER));
        assertEquals(notACylonCount,
                loyaltyCards.stream()
                        .filter(c -> !c.isCylon())
                        .filter(c -> c != MUTINEER)
                        .count());
        assertEquals(youAreACylonCount,
                loyaltyCards.stream()
                        .filter(LoyaltyCard::isCylon)
                        .count());
    }
}