package xyz.zlatanov.frakkintoasters.operation;

import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsDiscarded;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.TitleFact.*;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.QUORUM;

public class TitleOps extends GameOps {
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

    public void discardQuorumCards(List<QuorumCard> cards) {
        record(new PresidentQuorumCardsLost(cards));
        record(new CardsDiscarded(QUORUM, cards));
    }

}
