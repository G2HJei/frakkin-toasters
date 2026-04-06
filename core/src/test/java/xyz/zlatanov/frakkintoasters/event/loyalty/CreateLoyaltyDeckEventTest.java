package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.Player;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;
import xyz.zlatanov.frakkintoasters.state.deck.FakeDeck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.MUTINEER;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class CreateLoyaltyDeckEventTest {


    static Stream<Arguments> simpleLoyaltyDeckParams() {
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

    static Game game(int players) {
        return new Game(KOBOL, players);
    }

    @ParameterizedTest
    @MethodSource("simpleLoyaltyDeckParams")
    void shouldCreateSimpleLoyaltyDeck(Game game, boolean pickCylonLeader, int youAreACylonCount, int notACylonCount, boolean hasMutineer) {
        pickCharacters(game, pickCylonLeader);
        new CreateLoyaltyDeckEvent().execute(game);
        assertLoyalties(notACylonCount, youAreACylonCount, hasMutineer, game);
    }

    @Test
    void shouldAddExtraNotCylonCardsForBoomerAndGaius() {
        val game = game(3);
        game.player(1).selectCharacter(KARA_STARBUCK_THRACE);
        game.player(2).selectCharacter(GAIUS_BALTAR);
        game.player(3).selectCharacter(SHARON_BOOMER_VALERII);

        new CreateLoyaltyDeckEvent().execute(game);

        assertLoyalties(8, 1, false, game);
    }

    @Test
    void shouldDealMotiveCardsToCylonLeader() {
        val game = game(4);
        pickCharacters(game, true);

        new CreateLoyaltyDeckEvent().execute(game);

        assertEquals(2, game.player(4).motiveCards().size());
    }

    @Test
    void shouldDealLoyaltyCards() {
        val game = game(3);
        game.player(1).selectCharacter(GAIUS_BALTAR);
        game.player(2).selectCharacter(KARL_HELO_AGATHON);
        game.player(3).selectCharacter(SHARON_BOOMER_VALERII);

        new CreateLoyaltyDeckEvent().execute(game);

        assertEquals(2, game.player(1).loyaltyCards().size());
        assertEquals(1, game.player(2).loyaltyCards().size());
        assertEquals(1, game.player(3).loyaltyCards().size());
    }

    @Test
    void shouldFollowUpWithRevealMutineerAction() {
        val loyaltyDeck = new FakeDeck<LoyaltyCard>();
        loyaltyDeck.nextCard = MUTINEER;
        val game = new Game(KOBOL, 4,
                DecksHolder.builder()
                        .loyalty(loyaltyDeck)
                        .build());
        pickCharacters(game, false);

        val followup = new CreateLoyaltyDeckEvent().execute(game);

        assertEquals(List.of(new RevealMutineerEvent()), followup);
    }

    static void pickCharacters(Game game, boolean pickCylonLeader) {
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

    static void assertLoyalties(int notACylonCount, int youAreACylonCount, boolean mutineer, Game game) {
        val loyaltyCards = new ArrayList<>(game.decks().loyalty().cards());
        loyaltyCards.addAll(
                game.players()
                        .stream()
                        .map(Player::loyaltyCards)
                        .map(Deck::cards)
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