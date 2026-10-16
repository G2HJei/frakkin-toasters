package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheck;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparing;

public class SkillCheckOps extends GameOps {

    SkillCheckOps(Game game) {
        super(game);
    }

    public void createDestinyDeck() {
        val decks = game.decks();
        decks.destiny()
                .addOnTop(decks.politics().draw(2))
                .addOnTop(decks.leadership().draw(2))
                .addOnTop(decks.tactics().draw(2))
                .addOnTop(decks.piloting().draw(2))
                .addOnTop(decks.engineering().draw(2))
                .addOnTop(decks.treachery().draw(2));
        decks.destiny().shuffle();
    }

    public void start(SkillCheck skillCheck) {
        game.startSkillCheck(skillCheck);
    }

    public void playFromDestinyDeck(int count) { //todo always 2? check rules
        val destinyDeck = game.decks().destiny();
        val cards = destinyDeck.draw(count);
        if (destinyDeck.isEmpty()) {
            createDestinyDeck();
        }
        playCards(cards);
    }

    public void playCards(List<SkillCard> cards) {
        game.activeSkillCheck().cards().addOnTop(cards);
    }

    public List<SkillCard> shuffleAndDivideCards() {
        val cards = drawAllAndSort();
        splitIntoPiles(cards);
        return List.copyOf(cards);
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
        val skillCheck = game.activeSkillCheck();
        val matchingColors = skillCheck.type().skillCheckColors();
        for (val card : sortedCards) {
            val cardMatchesSkillCheck = matchingColors.contains(card.type().color());
            if (cardMatchesSkillCheck) {
                skillCheck.matchingPile().add(card);
            } else {
                skillCheck.nonMatchingPile().add(card);
            }
        }
    }
}
