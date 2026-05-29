package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.event.EventTest;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Player;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.MUTINEER;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class CreateLoyaltyDeckEventTest extends EventTest {


    static Stream<Arguments> simpleLoyaltyDeckParams() {
        return Stream.of(
                argumentSet("3p              ", 3, false, 1, 6, false),
                argumentSet("4p              ", 4, false, 1, 8, true),
                argumentSet("4p, cylon leader", 4, true, 1, 6, false),
                argumentSet("5p              ", 5, false, 2, 9, false),
                argumentSet("5p, cylon leader", 5, true, 1, 8, true),
                argumentSet("6p              ", 6, false, 2, 11, true),
                argumentSet("6p, cylon leader", 6, true, 2, 9, false),
                argumentSet("7p              ", 7, true, 2, 11, true)
        );
    }

    static Game withPlayers(int playerCount) {
        return Game.builder(playerCount).build();
    }

    @ParameterizedTest
    @MethodSource("simpleLoyaltyDeckParams")
    void shouldCreateSimpleLoyaltyDeck(int playerCount, boolean pickCylonLeader, int youAreACylonCount, int notACylonCount, boolean hasMutineer) {
        setUpGame(withPlayers(playerCount));
        pickCharacters(pickCylonLeader);
        execute(new CreateLoyaltyDeckEvent());
        assertLoyalties(notACylonCount, youAreACylonCount, hasMutineer);
    }

    @Test
    void shouldAddExtraNotCylonCardsForBoomerAndGaius() {
        game.player(1).selectCharacter(KARA_STARBUCK_THRACE);
        game.player(2).selectCharacter(GAIUS_BALTAR);
        game.player(3).selectCharacter(SHARON_BOOMER_VALERII);

        execute(new CreateLoyaltyDeckEvent());

        assertLoyalties(8, 1, false);
    }

    @Test
    void shouldDealMotiveCardsToCylonLeader() {
        setUpGame(withPlayers(4));
        pickCharacters(true);

        execute(new CreateLoyaltyDeckEvent());

        assertEquals(2, game.player(4).motiveCards().size());
    }

    @Test
    void shouldDealLoyaltyCards() {
        setUpGame(withPlayers(3));
        game.player(1).selectCharacter(GAIUS_BALTAR);
        game.player(2).selectCharacter(KARL_HELO_AGATHON);
        game.player(3).selectCharacter(SHARON_BOOMER_VALERII);

        execute(new CreateLoyaltyDeckEvent());

        assertEquals(2, game.player(1).loyaltyCards().size());
        assertEquals(1, game.player(2).loyaltyCards().size());
        assertEquals(1, game.player(3).loyaltyCards().size());
    }

    @Test
    void shouldFollowUpWithRevealMutineerAction() {
        setUpGame(withPlayers(4));
        loyaltyDeck.nextCard(MUTINEER);
        pickCharacters(false);

        val followup = execute(new CreateLoyaltyDeckEvent());

        assertEquals(single(new RevealMutineerEvent()), followup);
    }

    void pickCharacters(boolean pickCylonLeader) {
        player(1).selectCharacter(KARA_STARBUCK_THRACE);
        player(2).selectCharacter(WILLIAM_ADAMA);
        player(3).selectCharacter(LAURA_ROSLIN);
        val playerCount = game.players().size();
        if (playerCount > 3) {
            player(4).selectCharacter(pickCylonLeader ? CAVIL : CHIEF_GALEN_TYROL);
        }
        if (playerCount > 4) {
            player(5).selectCharacter(CALLANDRA_CALLY_TYROL);
        }
        if (playerCount > 5) {
            player(6).selectCharacter(ANASTASIA_DEE_DUALLA);
        }
        if (playerCount == 7) {
            player(7).selectCharacter(SHERMAN_DOC_COTTLE);
        }
    }

    void assertLoyalties(int notACylonCount, int youAreACylonCount, boolean mutineer) {
        val loyaltyCards = new ArrayList<>(loyaltyDeck.cards());
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
