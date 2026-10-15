package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;

import java.util.Arrays;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;

public class LoyaltyDeckOps extends AbstractOps {

    LoyaltyDeckOps(Game game) {
        super(game);
    }


    public LoyaltyDeckOps createNotACylon() {
        val deck = game.decks().loyaltyNotCylon();
        deck.addOnTop(
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
        return this;
    }

    public LoyaltyDeckOps create(int notCylonDraws, int cylonDraws, boolean withMutineer) {
        val loyaltyDeck = game.decks().loyalty();
        for (int i = 0; i < notCylonDraws; i++) {
            val notACylonLoyalty = game.decks().loyaltyNotCylon().draw();
            loyaltyDeck.addOnTop(notACylonLoyalty);
        }
        val cylonDeck = setupCylonDeck();
        for (int i = 0; i < cylonDraws; i++) {
            loyaltyDeck.addOnTop(cylonDeck.draw());
        }
        if (withMutineer) {
            loyaltyDeck.addOnTop(MUTINEER);
        }
        loyaltyDeck.shuffle();
        return this;
    }

    private Deck<LoyaltyCard> setupCylonDeck() {
        val deck = new Deck<LoyaltyCard>();
        val cylonLoyalties = Arrays.stream(values())
                .filter(LoyaltyCard::isCylon)
                .toList();
        deck.addOnTop(cylonLoyalties);
        deck.shuffle();
        return deck;
    }
}
