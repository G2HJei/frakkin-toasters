package xyz.zlatanov.frakkintoasters.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.Player;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;

import java.util.Arrays;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public record CreateLoyaltyDeckAction() implements Action {


    @Override
    public void apply(Game game) {
        val playerCount = game.players().size();
        val selectedCharacters = getSelectedCharacters(game);
        val hasCylonLeader = selectedCharacters.stream().anyMatch(c -> c.type() == CYLON_LEADER);
        val notCylonDeck = setupLoyaltyNotCylonDeck(game);
        val cylonDeck = setupCylonDeck();
        //todo handle gaius and boomer
        notCylonDeck.shuffle();
        cylonDeck.shuffle();
        val loyaltyDeck = game.decks().loyalty();
        createLoyaltyDeck(playerCount, hasCylonLeader, loyaltyDeck, notCylonDeck, cylonDeck);
        if (addMutineer(playerCount, hasCylonLeader)) {
            loyaltyDeck.add(MUTINEER);
        }
        loyaltyDeck.shuffle();
    }

    private List<Character> getSelectedCharacters(Game game) {
        return game.players().values()
                .stream()
                .map(Player::character)
                .toList();
    }

    private Deck<LoyaltyCard> setupLoyaltyNotCylonDeck(Game game) {
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
        return deck;
    }

    private Deck<LoyaltyCard> setupCylonDeck() {
        val deck = new Deck<LoyaltyCard>();
        val cylonLoyalties = Arrays.stream(values())
                .filter(LoyaltyCard::isCylon)
                .toList();
        deck.add(cylonLoyalties);
        return deck;
    }

    private boolean addMutineer(int playerCount, boolean hasCylonLeader) {
        return playerCount == 4 && !hasCylonLeader ||
                playerCount == 5 && hasCylonLeader ||
                playerCount == 6 && !hasCylonLeader ||
                playerCount == 7;
    }

    private void createLoyaltyDeck(int playerCount, boolean hasCylonLeader, Deck<LoyaltyCard> loyaltyDeck, Deck<LoyaltyCard> notCylonDeck, Deck<LoyaltyCard> cylonDeck) {
        for (int i = 0; i < notCylonDraws(playerCount, hasCylonLeader); i++) {
            loyaltyDeck.add(notCylonDeck.draw());
        }
        for (int i = 0; i < cylonDraws(playerCount, hasCylonLeader); i++) {
            loyaltyDeck.add(cylonDeck.draw());
        }
    }

    private int notCylonDraws(int playerCount, boolean hasCylonLeader) {
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

    private int cylonDraws(int playerCount, boolean hasCylonLeader) {
        if (playerCount == 5 && hasCylonLeader
                || playerCount < 5) {
            return 1;
        } else {
            return 2;
        }
    }
}
