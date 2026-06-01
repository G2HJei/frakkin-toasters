package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.Player;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.GAIUS_BALTAR;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SHARON_BOOMER_VALERII;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;
import static xyz.zlatanov.frakkintoasters.state.util.AllCardsProvider.genericDeck;

public class CreateLoyaltyDeckEventProcessor extends EventProcessor<CreateLoyaltyDeckEvent> {

    @Override
    public Followup process() {
        setupLoyaltyNotCylonDeck();

        val cylonDeck = setupCylonDeck();
        createLoyaltyDeck(cylonDeck);

        addMutineer();
        addExtraCards();

        game.decks().loyalty().shuffle();

        dealLoyaltyCards();
        dealMotiveCards();

        return followup();
    }

    private Followup followup() {
        val hasMutineer = game.players()
                .stream()
                .map(Player::loyaltyCards)
                .map(Deck::cards)
                .flatMap(Collection::stream)
                .anyMatch(MUTINEER::equals);
        return hasMutineer
                ? single(new RevealMutineerEvent())
                : Followup.NONE;
    }

    private void setupLoyaltyNotCylonDeck() {
        val deck = game.decks().loyaltyNotCylon();
        deck.add(
                //todo check correct vanilla non-cylon cards count
                List.of(NOT_CYLON, NOT_CYLON, NOT_CYLON, NOT_CYLON, NOT_CYLON, NOT_CYLON, NOT_CYLON, NOT_CYLON, NOT_CYLON, NOT_CYLON, NOT_CYLON,
                        NOT_CYLON_USE_CAUTION,
                        NOT_CYLON_STAND_AND_FIGHT,
                        NOT_CYLON_SELFISH,
                        NOT_CYLON_SELF_DESTRUCTION,
                        NOT_CYLON_SACRIFICE,
                        NOT_CYLON_POLITICAL_INTRIGUE,
                        NOT_CYLON_DEVASTATION,
                        NOT_CYLON_ACQUIRE_POWER,
                        FINAL_FIVE_YOU_ARE_SENT_TO_BRIG,
                        FINAL_FIVE_YOU_ARE_EXECUTED,
                        FINAL_FIVE_THEY_ARE_EXECUTED,
                        FINAL_FIVE_DAMAGE_GALACTICA_TWICE,
                        FINAL_FIVE_CYLON_SHIPS_ACTIVATE)
        );
        deck.shuffle();
    }

    private Deck<LoyaltyCard> setupCylonDeck() {
        val deck = new Deck<LoyaltyCard>();
        val cylonLoyalties = Arrays.stream(values())
                .filter(LoyaltyCard::isCylon)
                .toList();
        deck.add(cylonLoyalties);
        deck.shuffle();
        return deck;
    }

    private void createLoyaltyDeck(Deck<LoyaltyCard> cylonDeck) {
        val playerCount = game.players().size();
        val hasCylonLeader = hasCylonLeader();
        val loyaltyDeck = game.decks().loyalty();
        for (int i = 0; i < notCylonDraws(playerCount, hasCylonLeader); i++) {
            loyaltyDeck.add(game.decks().loyaltyNotCylon().draw());
        }
        for (int i = 0; i < cylonDraws(playerCount, hasCylonLeader); i++) {
            loyaltyDeck.add(cylonDeck.draw());
        }
    }

    private void addMutineer() {
        val playerCount = game.players().size();
        val hasCylonLeader = hasCylonLeader();
        val shouldAddMutineer = playerCount == 4 && !hasCylonLeader ||
                playerCount == 5 && hasCylonLeader ||
                playerCount == 6 && !hasCylonLeader ||
                playerCount == 7;
        if (shouldAddMutineer) {
            game.decks().loyalty().add(MUTINEER);
        }
    }

    private void addExtraCards() {
        val selectedCharacters = getSelectedCharacters();
        val loyaltyDeck = game.decks().loyalty();
        val notACylonDeck = game.decks().loyaltyNotCylon();
        if (selectedCharacters.contains(SHARON_BOOMER_VALERII)) {
            loyaltyDeck.add(notACylonDeck.draw());
        }
        if (selectedCharacters.contains(GAIUS_BALTAR)) {
            loyaltyDeck.add(notACylonDeck.draw());
        }
    }

    private void dealLoyaltyCards() {
        for (val player : game.players()) {
            val cardsToDraw = player.character() == GAIUS_BALTAR ? 2 : 1;
            val loyaltyCards = game.decks().loyalty().draw(cardsToDraw);
            player.loyaltyCards().add(loyaltyCards);
        }
    }

    private void dealMotiveCards() {
        if (!hasCylonLeader()) {
            return;
        }
        val cylonPlayer = game.players()
                .stream()
                .filter(p -> p.character().type() == CYLON_LEADER)
                .findFirst()
                .orElseThrow(FrakCallTheAdmiralException::new);
        val motiveCards = genericDeck(MotiveCard.class).draw(2);
        cylonPlayer.motiveCards().add(motiveCards);
    }

    private List<Character> getSelectedCharacters() {
        return game.players()
                .stream()
                .map(Player::character)
                .toList();
    }

    private boolean hasCylonLeader() {
        return getSelectedCharacters()
                .stream()
                .anyMatch(c -> c.type() == CYLON_LEADER);
    }

    private static int notCylonDraws(int playerCount, boolean hasCylonLeader) {
        if (playerCount == 3
                || playerCount == 4 && hasCylonLeader) {
            return 6;
        } else if (playerCount == 4
                || playerCount == 5 && hasCylonLeader) {
            return 8;
        } else if (playerCount == 5
                || playerCount == 6 && hasCylonLeader) {
            return 9;
        } else {
            return 11;
        }
    }

    private static int cylonDraws(int playerCount, boolean hasCylonLeader) {
        if (playerCount == 5 && hasCylonLeader
                || playerCount < 5) {
            return 1;
        } else {
            return 2;
        }
    }
}
