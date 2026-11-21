package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsDiscarded;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsRemovedFromGame;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.TitleFact.*;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCardInPlay;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.QUORUM;

public class TitleOps extends GameOps {

    // The President's Quorum hand, whatever his character's Skill Card limit is (rules 4.4.1.1, 8.8).
    public static final int QUORUM_HAND_LIMIT = 10;

    public TitleOps(Game game, FactRecorder recorder) {
        super(game, recorder);
    }

    public void assignAdmiral(Character character) {
        record(new AdmiralAssigned(character));
    }

    public void assignCag(Character character) {
        record(new CagAssigned(character));
    }

    public void assignPresident(Character character) {
        record(new PresidentAssigned(character));
    }

    public void drawQuorumCard() {
        QuorumCard card = draw(QUORUM);
        record(new PresidentQuorumCardGained(card));
    }

    /**
     * The President discards random Quorum Cards, as many as he has up to {@code count}.
     */
    public void discardRandomQuorumCards(int count) {
        val cards = game.randomness().shuffled(game.presidentHand().cards()).stream().limit(count).toList();
        if (!cards.isEmpty()) {
            discardQuorumCards(cards);
        }
    }

    public void discardQuorumCards(List<QuorumCard> cards) {
        record(new PresidentQuorumCardsLost(cards));
        record(new CardsDiscarded(QUORUM, cards));
    }

    /**
     * Takes the card out of the President's hand to play it. It is in no pile until its text says where it goes.
     */
    public void play(QuorumCard card) {
        record(new PresidentQuorumCardsLost(List.of(card)));
    }

    public void discardPlayedQuorumCard(QuorumCard card) {
        record(new CardsDiscarded(QUORUM, List.of(card)));
    }

    public void putInPlay(QuorumCard card, Integer holderPlayerNumber) {
        record(new QuorumCardPutInPlay(card, holderPlayerNumber));
    }

    public void discardFromPlay(QuorumCard card) {
        record(new QuorumCardLeftPlay(card));
        record(new CardsDiscarded(QUORUM, List.of(card)));
    }

    /**
     * Discards the Quorum Cards given to the player, e.g. when he is executed (rule 7.1) or stops Infiltrating (rule
     * 6.2.3.5).
     */
    public void discardQuorumCardsGivenTo(int playerNumber) {
        game.quorumCardsInPlay().stream()
                .filter(inPlay -> inPlay.holderPlayerNumber() != null && inPlay.holderPlayerNumber() == playerNumber)
                .map(QuorumCardInPlay::card)
                .toList()
                .forEach(this::discardFromPlay);
    }

    public void removeQuorumCardFromGame(QuorumCard card) {
        record(new CardsRemovedFromGame(QUORUM, List.of(card)));
    }

}
