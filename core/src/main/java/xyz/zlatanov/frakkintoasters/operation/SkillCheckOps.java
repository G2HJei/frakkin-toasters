package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsAddedOnTop;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.DeckShuffled;
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

    public void discardFromDestinyDeck(List<SkillCard> cards) {
        val remaining = new ArrayList<>(game.decks().destiny().cards());
        cards.forEach(remaining::remove);
        cards.forEach(this::discardToDeck);
        record(new DeckShuffled(DeckId.DESTINY, game.randomness().shuffled(remaining)));
        if (remaining.isEmpty()) {
            createDestinyDeck();
        }
    }

    public SkillCheckOps start(SkillCheckType type) {
        record(new SkillCheckStarted(type));
        return this;
    }

    /**
     * Starts the skill check of a Super Crisis Card that a revealed Cylon played.
     */
    public SkillCheckOps startSuperCrisis(SkillCheckType type, int playedBy) {
        record(new SkillCheckStarted(type));
        record(new PlayedBy(playedBy));
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

    // "Investigative Committee": everyone learns which cards the player put into the check.
    public void show(int playerNumber, List<SkillCard> cards) {
        record(new CardsShown(playerNumber, cards));
    }

    public List<SkillCard> playFromDestinyDeck(int count) {
        return playFromDestinyDeck(count, false);
    }

    public List<SkillCard> playFromDestinyDeck(int count, boolean faceUp) {
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
            // Face down until the shuffle, even onto an empty pile ("Guts and Initiative" skips the Destiny cards). After it,
            // face up on its pile, e.g. "Force Their Hand".
            if (game.activeSkillCheck().cardsDivided()) {
                addOnTopOfPile(card);
            } else {
                record(new CardsPlaced(CARDS, List.of(card), true));
            }
        }
    }

    public List<SkillCard> shuffleAndDivideCards() {
        val cards = drawAllAndSort();
        splitIntoPiles(cards);
        record(new CardsDivided());
        return cards;
    }

    public void discard(List<SkillCard> cards) {
        cards.forEach(this::discard);
    }

    public void discard(SkillCard card) {
        record(new CardRemoved(card, null));
        discardToDeck(card);
    }

    /**
     * The player takes a revealed card of the check into his hand ("Command Authority"). The card leaves its pile, not
     * the cards he played before the check.
     */
    public void takeRevealedCard(SkillCard card, int playerNumber) {
        record(new CardRemoved(card, null));
        player(playerNumber).receiveSkillCard(card);
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

    public void adjustDifficulty(int delta) {
        record(new DifficultyAdjusted(delta));
    }

    public void makePoliticsCountPositive() {
        record(new PoliticsCountPositive());
    }

    public void zeroStrength(SkillCardColor color) {
        record(new StrengthZeroed(color));
    }

    /**
     * Puts cards from the player's hand into the check after its cards were revealed ("Human Delusion"): face up, each
     * on its pile.
     */
    public void addRevealedCards(int playerNumber, List<SkillCard> cards) {
        for (val card : cards) {
            record(new CardsLost(playerNumber, PlayerPile.SKILL, List.of(card)));
            addOnTopOfPile(card);
        }
    }

    public void lookAt(int playerNumber, int targetPlayerNumber, List<SkillCard> cards) {
        record(new CardsLookedAt(playerNumber, targetPlayerNumber, cards));
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

    /**
     * Discards the whole Destiny deck and builds a new one from {@code count} cards of the color, as many as are left
     * ("Psychological Warfare"). Once it runs out, the next one is built as usual.
     */
    public void replaceDestinyDeck(SkillCardColor color, int count) {
        val destiny = game.decks().destiny().cards();
        destiny.forEach(this::discardToDeck);
        record(new DeckShuffled(DeckId.DESTINY, List.of()));
        List<SkillCard> cards = draw(DeckId.of(color), Math.min(count, game.decks().skillCardsLeft(color)));
        if (cards.isEmpty()) {
            createDestinyDeck();
            return;
        }
        record(new CardsAddedOnTop(DeckId.DESTINY, cards));
        shuffle(DeckId.DESTINY);
    }

    public void shuffleIntoDestinyDeck(SkillCardColor... skillCardColor) {
        val selection = computeColorSelection(skillCardColor);
        selection.forEach(
                (color, count) -> {
                    // A color with no cards left in its deck and discard pile can't be drawn (rule 8.2.2.5).
                    List<SkillCard> cards = draw(DeckId.of(color), Math.min(count, game.decks().skillCardsLeft(color)));
                    if (!cards.isEmpty()) {
                        record(new CardsAddedOnTop(DeckId.DESTINY, cards));
                    }
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
