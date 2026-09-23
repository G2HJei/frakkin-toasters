package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.deck.CreateDestinyDeckEventProcessor.createNewDestinyDeck;

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
            createNewDestinyDeck(game);
        }
        return cards;
    }

    private void addToSkillCheck(List<SkillCard> destinyCards) {
        game.activeSkillCheck().cards().addOnTop(destinyCards);
    }

}
