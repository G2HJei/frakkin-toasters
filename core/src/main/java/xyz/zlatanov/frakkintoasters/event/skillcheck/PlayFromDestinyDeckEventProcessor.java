package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

public class PlayFromDestinyDeckEventProcessor extends EventProcessor<PlayFromDestinyDeckEvent> {

    @Override
    public Followup process() {
        val destinyCards = drawDestinyCards();
        addToSkillCheck(destinyCards);
        return Followup.NONE;
    }

    private List<SkillCard> drawDestinyCards() {
        val destinyDeck = game.decks().destiny();
        val cards = destinyDeck.draw(2);
        if (destinyDeck.isEmpty()) {
            createNewDestinyDeck();
        }
        return cards;
    }

    private void addToSkillCheck(List<SkillCard> destinyCards) {
        game.activeSkillCheck().cards().addOnTop(destinyCards);
    }

    private void createNewDestinyDeck() {
        val decks = game.decks();
        game.decks().destiny()
                .addOnTop(decks.politics().draw(2))
                .addOnTop(decks.leadership().draw(2))
                .addOnTop(decks.tactics().draw(2))
                .addOnTop(decks.piloting().draw(2))
                .addOnTop(decks.engineering().draw(2))
                .addOnTop(decks.treachery().draw(2));
        game.decks().destiny().shuffle();
    }
}
