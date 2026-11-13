package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsAddedOnTop;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.CardsLost;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.SkillCheckFact.*;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.skill.*;

import java.util.*;

import static java.util.Comparator.comparing;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckPile.*;

public class SkillCheckOps extends GameOps {

    SkillCheckOps(Game game, FactRecorder recorder) {
        super(game, recorder);
    }

    public SkillCheckOps createDestinyDeck() {
        for (val color : SkillCardColor.values()) {
            List<SkillCard> cards = draw(DeckId.of(color), 2);
            record(new CardsAddedOnTop(DeckId.DESTINY, cards));
        }
        shuffle(DeckId.DESTINY);
        return this;
    }

    public SkillCheckOps start(SkillCheckType type) {
        record(new SkillCheckStarted(type));
        return this;
    }

    public SkillCheckOps start(SkillCheckType type, int targetPlayerNumber) {
        record(new SkillCheckStarted(type));
        record(new TargetChosen(targetPlayerNumber));
        return this;
    }

    public void playBeforeSkillCheck(int playerNumber, List<SkillCard> skillCards) {
        record(new CardsLost(playerNumber, PlayerPile.SKILL, skillCards));
        record(new CardsPlayedBefore(playerNumber, skillCards));
    }

    public void revealPlayedBefore() {
        record(new PlayedBeforeRevealed());
    }

    public List<SkillCard> playFromDestinyDeck(int count) {
        return playFromDestinyDeck(count, false);
    }

    public List<SkillCard> playFromDestinyDeck(int count, boolean faceUp) { //todo always 2? check rules
        List<SkillCard> cards = draw(DeckId.DESTINY, count);
        if (faceUp) {
            cards.forEach(this::addOnTopOfPile);
        } else {
            record(new CardsPlaced(CARDS, cards, true));
        }
        if (game.decks().destiny().isEmpty()) {
            createDestinyDeck();
        }
        return cards;
    }

    public SkillCard revealAndDiscardTreacheryCard() {
        SkillCard card = draw(DeckId.TREACHERY);
        discardToDeck(card);
        return card;
    }

    public void playCards(int playerNumber, SkillCard... cards) {
        playCards(playerNumber, Arrays.stream(cards).toList());
    }

    public void playCards(int playerNumber, List<SkillCard> cards) {
        for (val card : cards) {
            record(new CardsLost(playerNumber, PlayerPile.SKILL, List.of(card)));
            if (game.activeSkillCheck().cards().isEmpty()) {
                addOnTopOfPile(card);
            } else {
                record(new CardsPlaced(CARDS, List.of(card), true));
            }
        }
    }

    public List<SkillCard> shuffleAndDivideCards() {
        val cards = drawAllAndSort();
        splitIntoPiles(cards);
        return cards;
    }

    public void discard(List<SkillCard> cards) {
        cards.forEach(this::discard);
    }

    public void discard(SkillCard card) {
        record(new CardRemoved(card, null));
        discardToDeck(card);
    }

    public void giveCardToPlayer(SkillCard card, int playerNumber) {
        record(new CardRemoved(card, playerNumber));
        player(playerNumber).receiveSkillCard(card);
    }

    public void resolveAbility(SkillCardType type) {
        record(new AbilityResolved(type));
    }

    public void declareEmergency() {
        record(new EmergencyDeclared());
    }

    public void totalStrength(int totalStrength) {
        record(new TotalStrengthDetermined(totalStrength));
    }

    public void determineResult(SkillCheckResult result) {
        record(new ResultDetermined(result));
    }

    public void replacePassResult() {
        record(new PassResultReplaced());
    }

    public void preventFailResult() {
        record(new FailResultPrevented());
    }

    public void end() {
        val skillCheck = game.activeSkillCheck();
        // All Skill Cards of the check go to their discard piles (rule 10.3.1), the cards played before it too.
        for (val player : game.players()) {
            for (val card : skillCheck.playedBeforeSkillCheck(player.number())) {
                record(new CardRemoved(card, player.number()));
                discardToDeck(card);
            }
        }
        // Face-down cards are left only when the check wasn't made, e.g. after "Political Prowess".
        val faceDown = skillCheck.cards().cards();
        if (!faceDown.isEmpty()) {
            record(new CardsDrawn(CARDS, faceDown));
            faceDown.forEach(this::discardToDeck);
        }
        skillCheck.dividedCards().forEach(this::discard);
        record(new SkillCheckEnded());
    }

    private List<SkillCard> drawAllAndSort() {
        val all = new ArrayList<>(game.activeSkillCheck().cards().cards());
        if (!all.isEmpty()) {
            record(new CardsDrawn(CARDS, all));
        }
        all.sort(comparing((SkillCard card) -> card.type().color())
                .thenComparing(SkillCard::value, Comparator.reverseOrder())
                .thenComparing(SkillCard::type, Comparator.reverseOrder()));
        return all;
    }

    private void splitIntoPiles(List<SkillCard> sortedCards) {
        for (val card : sortedCards) {
            record(new CardsPlaced(pileOf(card), List.of(card), false));
        }
    }

    private void addOnTopOfPile(SkillCard card) {
        record(new CardsPlaced(pileOf(card), List.of(card), true));
    }

    private SkillCheckPile pileOf(SkillCard card) {
        return matchesSkillCheckColors(card) ? MATCHING : NON_MATCHING;
    }

    private boolean matchesSkillCheckColors(SkillCard card) {
        val cardColor = card.type().color();
        return game.activeSkillCheck().type().skillCheckColors().contains(cardColor);
    }

    public void shuffleIntoDestinyDeck(SkillCardColor... skillCardColor) {
        val selection = computeColorSelection(skillCardColor);
        selection.forEach(
                (color, count) -> {
                    List<SkillCard> cards = draw(DeckId.of(color), count);
                    record(new CardsAddedOnTop(DeckId.DESTINY, cards));
                });
        shuffle(DeckId.DESTINY);
    }

    private Map<SkillCardColor, Integer> computeColorSelection(SkillCardColor[] skillCardColor) {
        val selection = new TreeMap<SkillCardColor, Integer>();
        for (val color : skillCardColor) {
            selection.compute(color, (k, v) -> (v == null) ? 1 : v + 1);
        }
        return selection;
    }
}
