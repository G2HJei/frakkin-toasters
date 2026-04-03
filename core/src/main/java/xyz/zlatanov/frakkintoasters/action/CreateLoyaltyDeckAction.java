package xyz.zlatanov.frakkintoasters.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.Player;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.GAIUS_BALTAR;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SHARON_BOOMER_VALERII;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;
import static xyz.zlatanov.frakkintoasters.state.util.AllCardsProvider.genericDeck;

public record CreateLoyaltyDeckAction() implements Action {

    @Override
    public void apply(Game game) {
        setupLoyaltyNotCylonDeck(game);

        val cylonDeck = setupCylonDeck();
        createLoyaltyDeck(game, cylonDeck);

        addMutineer(game);
        addExtraCards(game);

        game.decks().loyalty().shuffle();

        dealLoyaltyCards(game);
        dealMotiveCards(game);
    }

    @Override
    public List<Action> followup(Game game) {
        val hasMutineer = game.players()
                .stream()
                .map(Player::loyaltyCards)
                .map(Deck::cards)
                .flatMap(Collection::stream)
                .anyMatch(MUTINEER::equals);
        return hasMutineer
                ? List.of(new RevealMutineerAction())
                : List.of();
    }

    private static void setupLoyaltyNotCylonDeck(Game game) {
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

    private static Deck<LoyaltyCard> setupCylonDeck() {
        val deck = new Deck<LoyaltyCard>();
        val cylonLoyalties = Arrays.stream(values())
                .filter(LoyaltyCard::isCylon)
                .toList();
        deck.add(cylonLoyalties);
        deck.shuffle();
        return deck;
    }

    private static void createLoyaltyDeck(Game game, Deck<LoyaltyCard> cylonDeck) {
        val playerCount = game.players().size();
        val hasCylonLeader = hasCylonLeader(game);
        val loyaltyDeck = game.decks().loyalty();
        for (int i = 0; i < notCylonDraws(playerCount, hasCylonLeader); i++) {
            loyaltyDeck.add(game.decks().loyaltyNotCylon().draw());
        }
        for (int i = 0; i < cylonDraws(playerCount, hasCylonLeader); i++) {
            loyaltyDeck.add(cylonDeck.draw());
        }
    }

    private static void addMutineer(Game game) {
        val playerCount = game.players().size();
        val hasCylonLeader = hasCylonLeader(game);
        val shouldAddMutineer = playerCount == 4 && !hasCylonLeader ||
                playerCount == 5 && hasCylonLeader ||
                playerCount == 6 && !hasCylonLeader ||
                playerCount == 7;
        if (shouldAddMutineer) {
            game.decks().loyalty().add(MUTINEER);
        }
    }

    private static void addExtraCards(Game game) {
        val selectedCharacters = getSelectedCharacters(game);
        val loyaltyDeck = game.decks().loyalty();
        val notACylonDeck = game.decks().loyaltyNotCylon();
        if (selectedCharacters.contains(SHARON_BOOMER_VALERII)) {
            loyaltyDeck.add(notACylonDeck.draw());
        }
        if (selectedCharacters.contains(GAIUS_BALTAR)) {
            loyaltyDeck.add(notACylonDeck.draw());
        }
    }

    private static void dealLoyaltyCards(Game game) {
        for (val player : game.players()) {
            val cardsToDraw = player.character() == GAIUS_BALTAR ? 2 : 1;
            val loyaltyCards = game.decks().loyalty().draw(cardsToDraw);
            player.loyaltyCards().add(loyaltyCards);
        }
    }

    private static void dealMotiveCards(Game game) {
        if (!hasCylonLeader(game)) {
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

    private static List<Character> getSelectedCharacters(Game game) {
        return game.players()
                .stream()
                .map(Player::character)
                .toList();
    }

    private static boolean hasCylonLeader(Game game) {
        return getSelectedCharacters(game)
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
