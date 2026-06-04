package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.Player;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.MUTINEER;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class CreateLoyaltyDeckEventProcessorTest extends EventTestHarness<CreateLoyaltyDeckEvent> {

    CreateLoyaltyDeckEvent event = new CreateLoyaltyDeckEvent();

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

    @ParameterizedTest
    @MethodSource("simpleLoyaltyDeckParams")
    void shouldCreateSimpleLoyaltyDeck(int playerCount, boolean pickCylonLeader, int youAreACylonCount, int notACylonCount, boolean hasMutineer) {
        setUpGame(playerCount);
        pickCharacters(pickCylonLeader);
        execute(event);
        assertLoyalties(notACylonCount, youAreACylonCount, hasMutineer);
    }

    @Test
    void shouldAddOnTopExtraNotCylonCardsForBoomerAndGaius() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(GAIUS_BALTAR);
        player(3).character(SHARON_BOOMER_VALERII);

        execute(event);

        assertNoFollowup();
        assertLoyalties(8, 1, false);
    }

    @Test
    void shouldDealMotiveCardsToCylonLeader() {
        setUpGame(4);
        pickCharacters(true);

        execute(event);

        assertNoFollowup();
        assertEquals(2, player(4).motiveCards().size());
    }

    @Test
    void shouldDealLoyaltyCards() {
        setUpGame(3);
        player(1).character(GAIUS_BALTAR);
        player(2).character(KARL_HELO_AGATHON);
        player(3).character(SHARON_BOOMER_VALERII);

        execute(event);

        assertNoFollowup();
        assertEquals(2, player(1).loyaltyCards().size());
        assertEquals(1, player(2).loyaltyCards().size());
        assertEquals(1, player(3).loyaltyCards().size());
    }

    @Test
    void shouldFollowUpWithRevealMutineerAction() {
        setUpGame(4);
        loyaltyDeck.nextCard(MUTINEER);
        pickCharacters(false);

        execute(event);

        assertFollowup(new RevealMutineerEvent());
    }

    void pickCharacters(boolean pickCylonLeader) {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
        val playerCount = game.players().size();
        if (playerCount > 3) {
            player(4).character(pickCylonLeader ? CAVIL : CHIEF_GALEN_TYROL);
        }
        if (playerCount > 4) {
            player(5).character(CALLANDRA_CALLY_TYROL);
        }
        if (playerCount > 5) {
            player(6).character(ANASTASIA_DEE_DUALLA);
        }
        if (playerCount == 7) {
            player(7).character(SHERMAN_DOC_COTTLE);
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
