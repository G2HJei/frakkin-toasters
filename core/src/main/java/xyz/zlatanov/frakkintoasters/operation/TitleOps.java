package xyz.zlatanov.frakkintoasters.operation;

import xyz.zlatanov.frakkintoasters.journal.fact.TitleFact.AdmiralAssigned;
import xyz.zlatanov.frakkintoasters.journal.fact.TitleFact.CagAssigned;
import xyz.zlatanov.frakkintoasters.journal.fact.TitleFact.PresidentAssigned;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.QUORUM;

public class TitleOps extends GameOps {
    public TitleOps(Game game) {
        super(game);
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
        game.presidentHand().addOnTop(card);
    }

}
