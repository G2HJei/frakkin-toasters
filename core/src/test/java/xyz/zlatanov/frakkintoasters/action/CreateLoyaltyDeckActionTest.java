package xyz.zlatanov.frakkintoasters.action;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.Player;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.MUTINEER;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class CreateLoyaltyDeckActionTest {


    private static Stream<Arguments> simpleLoyaltyDeckParams() {
        return Stream.of(
                argumentSet("3p              ", game(3), false, 1, 6, false),
                argumentSet("4p              ", game(4), false, 1, 8, true),
                argumentSet("4p, cylon leader", game(4), true, 1, 6, false),
                argumentSet("5p              ", game(5), false, 2, 9, false),
                argumentSet("5p, cylon leader", game(5), true, 1, 8, true),
                argumentSet("6p              ", game(6), false, 2, 11, true),
                argumentSet("6p, cylon leader", game(6), true, 2, 9, false),
                argumentSet("7p              ", game(7), true, 2, 11, true)
        );
    }

    private static Game game(int players) {
        return new Game(KOBOL, players);
    }

    @ParameterizedTest
    @MethodSource("simpleLoyaltyDeckParams")
    void shouldCreateSimpleLoyaltyDeck(Game game, boolean pickCylonLeader, int youAreACylonCount, int notACylonCount, boolean hasMutineer) {
        pickCharacters(game, pickCylonLeader);
        new CreateLoyaltyDeckAction().execute(game);
        assertDeckComposition(notACylonCount, youAreACylonCount, hasMutineer, game);
    }

    @Test
    void shouldAddNotCylonCardsForBoomerAndGaius() {
        val game = game(3);
        game.player(1).selectCharacter(KARA_STARBUCK_THRACE);
        game.player(2).selectCharacter(GAIUS_BALTAR);
        game.player(3).selectCharacter(SHARON_BOOMER_VALERII);

        new CreateLoyaltyDeckAction().execute(game);

        assertDeckComposition(8, 1, false, game);
    }

    @Test
    void shouldDistributeMotiveCardsToCylonLeader() {
        val game = game(4);
        pickCharacters(game, true);

        new CreateLoyaltyDeckAction().execute(game);

        assertEquals(2, game.player(4).motiveCards().size());
    }

    @Test
    void shouldDistributeLoyaltyCards() {

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

    private static void assertDeckComposition(int notACylonCount, int youAreACylonCount, boolean mutineer, Game game) {
        val loyaltyCards = new ArrayList<>(game.decks().loyalty().cards());
        loyaltyCards.addAll(
                game.players().values()
                        .stream()
                        .map(Player::loyaltyCards)
                        .flatMap(Collection::stream)
                        .toList());
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