package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparing;

public class SkillCheckOps extends GameOps {

    SkillCheckOps(Game game) {
        super(game);
    }

    public SkillCheckOps createDestinyDeck() {
        val decks = game.decks();
        decks.destiny()
                .addOnTop(decks.politics().draw(2))
                .addOnTop(decks.leadership().draw(2))
                .addOnTop(decks.tactics().draw(2))
                .addOnTop(decks.piloting().draw(2))
                .addOnTop(decks.engineering().draw(2))
                .addOnTop(decks.treachery().draw(2));
        decks.destiny().shuffle();
        return this;
    }

    public SkillCheckOps start(SkillCheckType type) {
        game.startSkillCheck(type);
        return this;
    }

    public void playFromDestinyDeck(int count) {
        playFromDestinyDeck(count, false);
    }

    public void playFromDestinyDeck(int count, boolean faceUp) { //todo always 2? check rules
        val destinyDeck = game.decks().destiny();
        val cards = destinyDeck.draw(count);
        if (faceUp) {
            cards.forEach(this::addOnTopOfPile);
        } else {
            game.activeSkillCheck().cards().addOnTop(cards);
        }
        if (destinyDeck.isEmpty()) {
            createDestinyDeck();
        }
    }

    public void playCards(int playerNumber, SkillCard... cards) {
        playCards(playerNumber, Arrays.stream(cards).toList());
    }

    public void playCards(int playerNumber, List<SkillCard> cards) {
        val skillCheck = game.activeSkillCheck();
        for (val card : cards) {
            game.player(playerNumber).skillCards().remove(card);
            if (skillCheck.cards().isEmpty()) {
                addOnTopOfPile(card);
            } else {
                skillCheck.cards().addOnTop(card);
            }
        }
    }

    public List<SkillCard> shuffleAndDivideCards() {
        val cards = drawAllAndSort();
        splitIntoPiles(cards);
        return List.copyOf(cards);
    }

    public void discard(List<SkillCard> cards) {
        cards.forEach(this::discard);
    }

    public void discard(SkillCard card) {
        game.activeSkillCheck().remove(card);
        game.decks().discard(card);
    }

    public void giveCardToPlayer(SkillCard card, int playerNumber) {
        game.activeSkillCheck().remove(card);
        player(playerNumber).receiveSkillCard(card);
    }

    private List<SkillCard> drawAllAndSort() {
        val deck = game.activeSkillCheck().cards();
        val all = new ArrayList<>(deck.draw(deck.size()));
        all.sort(comparing((SkillCard card) -> card.type().color())
                .thenComparing(SkillCard::value, Comparator.reverseOrder())
                .thenComparing(SkillCard::type, Comparator.reverseOrder()));
        return all;
    }

    private void splitIntoPiles(List<SkillCard> sortedCards) {
        for (val card : sortedCards) {
            addToBottomOfPile(card);
        }
    }

    private void addOnTopOfPile(SkillCard card) {
        if (matchesSkillCheckColors(card)) {
            game.activeSkillCheck().matchingPile().addOnTop(card);
        } else {
            game.activeSkillCheck().nonMatchingPile().addOnTop(card);
        }
    }

    private void addToBottomOfPile(SkillCard card) {
        if (matchesSkillCheckColors(card)) {
            game.activeSkillCheck().matchingPile().addToBottom(card);
        } else {
            game.activeSkillCheck().nonMatchingPile().addToBottom(card);
        }
    }

    private boolean matchesSkillCheckColors(SkillCard card) {
        val cardColor = card.type().color();
        return game.activeSkillCheck().type().skillCheckColors().contains(cardColor);
    }
}
