package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
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

    static Game withPlayers(int playerCount) {
        return Game.builder(playerCount).build();
    }

    @ParameterizedTest
    @MethodSource("simpleLoyaltyDeckParams")
    void shouldCreateSimpleLoyaltyDeck(int playerCount, boolean pickCylonLeader, int youAreACylonCount, int notACylonCount, boolean hasMutineer) {
        setUpGame(withPlayers(playerCount));
        pickCharacters(pickCylonLeader);
        execute(event);
        assertLoyalties(notACylonCount, youAreACylonCount, hasMutineer);
    }

    @Test
    void shouldAddOnTopExtraNotCylonCardsForBoomerAndGaius() {
        selectCharacter(1, KARA_STARBUCK_THRACE);
        selectCharacter(2, GAIUS_BALTAR);
        selectCharacter(3, SHARON_BOOMER_VALERII);

        executeAndAssertNoFollowup(event);

        assertLoyalties(8, 1, false);
    }

    @Test
    void shouldDealMotiveCardsToCylonLeader() {
        setUpGame(withPlayers(4));
        pickCharacters(true);

        executeAndAssertNoFollowup(event);

        assertEquals(2, player(4).motiveCards().size());
    }

    @Test
    void shouldDealLoyaltyCards() {
        setUpGame(withPlayers(3));
        selectCharacter(1, GAIUS_BALTAR);
        selectCharacter(2, KARL_HELO_AGATHON);
        selectCharacter(3, SHARON_BOOMER_VALERII);

        executeAndAssertNoFollowup(event);

        assertEquals(2, player(1).loyaltyCards().size());
        assertEquals(1, player(2).loyaltyCards().size());
        assertEquals(1, player(3).loyaltyCards().size());
    }

    @Test
    void shouldFollowUpWithRevealMutineerAction() {
        setUpGame(withPlayers(4));
        nextCard(loyaltyDeck, MUTINEER);
        pickCharacters(false);

        executeAndAssertFollowup(event, single(new RevealMutineerEvent()));
    }

    void pickCharacters(boolean pickCylonLeader) {
        selectCharacter(1, KARA_STARBUCK_THRACE);
        selectCharacter(2, WILLIAM_ADAMA);
        selectCharacter(3, LAURA_ROSLIN);
        val playerCount = game.players().size();
        if (playerCount > 3) {
            selectCharacter(4, pickCylonLeader ? CAVIL : CHIEF_GALEN_TYROL);
        }
        if (playerCount > 4) {
            selectCharacter(5, CALLANDRA_CALLY_TYROL);
        }
        if (playerCount > 5) {
            selectCharacter(6, ANASTASIA_DEE_DUALLA);
        }
        if (playerCount == 7) {
            selectCharacter(7, SHERMAN_DOC_COTTLE);
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
