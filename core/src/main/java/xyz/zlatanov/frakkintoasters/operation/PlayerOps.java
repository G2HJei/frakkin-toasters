package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsAddedOnTop;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsAddedToBottom;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsDiscarded;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsRemovedFromGame;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.SkillCardPlayed;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.ShipFact.PilotChanged;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Player;
import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class PlayerOps extends GameOps {

    private final Player player;

    PlayerOps(Game game, FactRecorder recorder, int playerNumber) {
        super(game, recorder);
        this.player = game.player(playerNumber);
    }

    public void selectCharacter(Character selectedCharacter) {
        record(new CharacterSelected(player.number(), selectedCharacter));
    }

    /**
     *  the cards drawn
     */
    public List<LoyaltyCard> drawLoyaltyCards(int cardsToDraw) {
        List<LoyaltyCard> loyaltyCards = draw(DeckId.LOYALTY, cardsToDraw);
        gain(PlayerPile.LOYALTY, loyaltyCards);
        return loyaltyCards;
    }

    // Into the "Brig" only through SendToBrigEvent, which passes the Admiral's and the CAG's titles on (rule 4.4).
    public void moveTo(Location location) {
        assert !location.isSpaceLocation();
        record(new CharacterMoved(player.character(), location));
    }

    public void sendTo(Location location) {
        galacticaBoard().pilotedFighter(player.character())
                .ifPresentOrElse(
                        fighter -> ship(fighter.id()).land(location),
                        () -> moveTo(location));
    }

    public void drawSkillCards(SkillCardColor... colors) {
        drawSkillCards(Arrays.stream(colors).toList());
    }

    /**
     * @return the cards drawn, in the order of the colors
     */
    public List<SkillCard> drawSkillCards(List<SkillCardColor> colors) {
        val drawn = new ArrayList<SkillCard>();
        for (val color : colors) {
            SkillCard card = draw(DeckId.of(color));
            gain(PlayerPile.SKILL, List.of(card));
            drawn.add(card);
        }
        return drawn;
    }

    public CrisisCard drawCrisisCard() {
        return draw(DeckId.CRISIS);
    }

    public PlayerOps drawSuperCrisisCard() {
        val card = draw(DeckId.SUPER_CRISIS);
        gain(PlayerPile.SUPER_CRISIS, List.of(card));
        return this;
    }

    /**
     * Draws Mutiny Cards to choose from, e.g. for Tom Zarek (alt.)'s "Abuse Power": they are in no pile and in no hand
     * until the caller says where they go, so drawing them sends nobody to the "Brig".
     */
    public List<MutinyCard> drawMutinyCardsToChoose(int count) {
        return draw(DeckId.MUTINY, count);
    }

    public void holdMutinyCard(MutinyCard card) {
        gain(PlayerPile.MUTINY, List.of(card));
    }

    public void putMutinyCardsAtBottom(List<MutinyCard> cards) {
        if (!cards.isEmpty()) {
            record(new CardsAddedToBottom(DeckId.MUTINY, cards));
        }
    }

    public void drawMutinyCard() {
        val card = draw(DeckId.MUTINY);
        gain(PlayerPile.MUTINY, List.of(card)); // DrawMutinyCardEventProcessor sends a player to the "Brig"
    }

    public void drawMotiveCards() {
        List<MotiveCard> motiveCards = draw(DeckId.MOTIVE, 2);
        gain(PlayerPile.MOTIVE, motiveCards);
    }

    public DestinationCard drawDestinationCard() {
        return draw(DeckId.DESTINATION);
    }

    public void placeCrisisCardOnTop(CrisisCard card) {
        record(new CardsAddedOnTop(DeckId.CRISIS, List.of(card)));
    }

    public void placeCrisisCardOnBottom(CrisisCard card) {
        record(new CardsAddedToBottom(DeckId.CRISIS, List.of(card)));
    }

    public void placeDestinationCardOnTop(DestinationCard card) {
        record(new CardsAddedOnTop(DeckId.DESTINATION, List.of(card)));
    }

    public void placeDestinationCardOnBottom(DestinationCard card) {
        record(new CardsAddedToBottom(DeckId.DESTINATION, List.of(card)));
    }

    public PlayerOps discard(SkillCard... cards) {
        if (cards.length > 0) {
            record(new CardsLost(player.number(), PlayerPile.SKILL, List.of(cards)));
            Arrays.stream(cards).forEach(this::discardToDeck);
        }
        return this;
    }

    /**
     * Plays a Skill Card from the hand for its ability, e.g. its "Action:". It goes to its discard pile before the
     * ability resolves.
     */
    public void play(SkillCard card) {
        record(new CardsLost(player.number(), PlayerPile.SKILL, List.of(card)));
        record(new SkillCardPlayed(player.number(), card));
        discardToDeck(card);
    }

    /**
     * Discards random Skill Cards from his hand, as many as he has up to {@code count}.
     */
    public void discardRandomSkillCards(int count) {
        val hand = game.randomness().shuffled(player.skillCards().cards());
        discard(hand.stream().limit(count).toArray(SkillCard[]::new));
    }

    /**
     * Looks at 1 random Loyalty Card of the target that isn't revealed, if he has one.
     *
     * @return the card seen, empty when he has none
     */
    public Optional<LoyaltyCard> lookAtLoyaltyCard(int targetPlayerNumber) {
        val cards = game.player(targetPlayerNumber).loyaltyCards().cards();
        if (cards.isEmpty()) {
            return Optional.empty();
        }
        val card = game.randomness().shuffled(cards).getFirst();
        record(new LoyaltyCardSeen(player.number(), targetPlayerNumber, card));
        return Optional.of(card);
    }

    /**
     * Looks at every Loyalty Card of the target that isn't revealed, e.g. for "Cylon Detector".
     *
     * @return the cards seen
     */
    public List<LoyaltyCard> lookAtAllLoyaltyCards(int targetPlayerNumber) {
        val cards = game.player(targetPlayerNumber).loyaltyCards().cards();
        cards.forEach(card -> record(new LoyaltyCardSeen(player.number(), targetPlayerNumber, card)));
        return cards;
    }

    /**
     * Shows one of his Final Five cards to everyone after another player looked at it (rule 5.4.1.1).
     */
    public void showFinalFiveCard(LoyaltyCard card) {
        record(new FinalFiveCardShown(player.number(), card));
    }

    /**
     * Takes Loyalty Cards out of the game, e.g. the Final Five cards an execution reveals (rule 5.4.1.2).
     */
    public void removeLoyaltyCardsFromGame(List<LoyaltyCard> cards) {
        if (!cards.isEmpty()) {
            record(new CardsLost(player.number(), PlayerPile.LOYALTY, cards));
            record(new CardsRemovedFromGame(DeckId.LOYALTY, cards));
        }
    }

    public void holdQuorumCard(QuorumCard card) {
        gain(PlayerPile.QUORUM, List.of(card));
    }

    /**
     * Takes a Quorum Card the player holds himself out of his pile to play it.
     */
    public void playHeldQuorumCard(QuorumCard card) {
        record(new CardsLost(player.number(), PlayerPile.QUORUM, List.of(card)));
    }

    public void discardHeldQuorumCard(QuorumCard card) {
        playHeldQuorumCard(card);
        discardToDeck(card);
    }

    /**
     * Gives Skill Cards from his hand to another player. Each of them sees the cards, the others only how many.
     */
    public void giveSkillCards(int recipientPlayerNumber, List<SkillCard> cards) {
        if (!cards.isEmpty()) {
            record(new CardsLost(player.number(), PlayerPile.SKILL, cards));
            record(new CardsGained(recipientPlayerNumber, PlayerPile.SKILL, cards));
        }
    }

    /**
     * Takes random Skill Cards from another player's hand, as many as he has up to {@code count}.
     */
    public void takeRandomSkillCards(int fromPlayerNumber, int count) {
        val hand = game.randomness().shuffled(game.player(fromPlayerNumber).skillCards().cards());
        player(fromPlayerNumber).giveSkillCards(player.number(), hand.stream().limit(count).toList());
    }

    /**
     * Takes a Mutiny Card out of his hand to play it. It is in no pile until its text says where it goes.
     */
    public void playMutinyCard(MutinyCard card) {
        record(new CardsLost(player.number(), PlayerPile.MUTINY, List.of(card)));
    }

    /**
     * Takes a Super Crisis Card out of his hand to play it at "Caprica". It is in no pile while it resolves.
     */
    public void playSuperCrisisCard(SuperCrisisCard card) {
        record(new CardsLost(player.number(), PlayerPile.SUPER_CRISIS, List.of(card)));
    }

    /**
     * Discards a Super Crisis Card from his hand without playing it, e.g. for D'Anna Biers' "Heretic".
     */
    public void discardSuperCrisisCard(SuperCrisisCard card) {
        playSuperCrisisCard(card);
        record(new CardsDiscarded(DeckId.SUPER_CRISIS, List.of(card)));
    }

    /**
     * Draws cards from the top of the Destiny deck into his hand, e.g. for Leoben Conoy's "Glimpse the Face of God". An
     * empty Destiny deck is built anew before the next card, as after a skill check.
     */
    public void drawFromDestinyDeck(int count) {
        for (int i = 0; i < count; i++) {
            if (game.decks().destiny().isEmpty()) {
                skillCheck().createDestinyDeck();
            }
            SkillCard card = draw(DeckId.DESTINY);
            gain(PlayerPile.SKILL, List.of(card));
        }
        if (game.decks().destiny().isEmpty()) {
            skillCheck().createDestinyDeck();
        }
    }

    /**
     * Puts Skill Cards from his hand on top of the Destiny deck, the first one on top.
     */
    public void placeOnDestinyDeck(List<SkillCard> cards) {
        if (!cards.isEmpty()) {
            record(new CardsLost(player.number(), PlayerPile.SKILL, cards));
            record(new CardsAddedOnTop(DeckId.DESTINY, cards));
        }
    }

    public void discard(MutinyCard... cards) {
        if (cards.length > 0) {
            record(new CardsLost(player.number(), PlayerPile.MUTINY, List.of(cards)));
            Arrays.stream(cards).forEach(this::discardToDeck);
        }
    }

    public void infiltrateGalactica(Location infiltrateLocation) {
        record(new InfiltrationStarted(player.number()));
        moveTo(infiltrateLocation);
    }

    public void lookAtShip(Integer civId) {
        record(new CivilianShipRevealed(player.number(), civId));
    }

    // The player looks at cards of the deck that he doesn't take, so only he knows them.
    public void see(DeckId deck, List<?> cards) {
        record(new CardsSeen(player.number(), deck, cards));
    }

    public void pilot(int humanFighterId) {
        val character = player.character();
        galacticaBoard().shipInSpace(humanFighterId, HumanFighter.class); // fails when there is no such fighter
        record(new CharacterRemovedFromBoards(character));
        record(new PilotChanged(humanFighterId, character));
    }

    public void receiveSkillCard(SkillCard card) {
        gain(PlayerPile.SKILL, List.of(card));
    }

    // "He can do so only if he does not currently have one" (rule 4.3.1), up to three for Gaius Baltar (alt.), and never
    // as a revealed Cylon (rule 4.3.2).
    /**
     * The ability that was triggered for this player has been offered, e.g. "Memento".
     */
    public void clearAbilityTrigger(CharacterAbility ability) {
        record(new AbilityTriggerCleared(player.number(), ability));
    }

    public void gainMiracleToken() {
        if (player.canGainMiracleToken()) {
            record(new MiracleTokenGained(player.number()));
        }
    }

    /**
     * Gives one of his miracle tokens to another player, who has room for it, e.g. Gaius Baltar (alt.)'s "Cult Leader".
     */
    public void giveMiracleToken(int recipientPlayerNumber) {
        record(new MiracleTokenLost(player.number()));
        player(recipientPlayerNumber).gainMiracleToken();
    }

    /**
     * Discards all of his miracle tokens, e.g. when he reveals as a Cylon or is executed.
     */
    public void loseMiracleTokens() {
        val tokens = player.miracleTokens(); // each fact applies at once
        for (int i = 0; i < tokens; i++) {
            record(new MiracleTokenLost(player.number()));
        }
    }

    /**
     * Uses an ability of his character sheet. A miracle ability spends its miracle tokens (rule 4.3).
     */
    public void useAbility(CharacterAbility ability) {
        record(new AbilityUsed(player.number(), ability));
        for (int i = 0; i < ability.miracleTokens(); i++) {
            record(new MiracleTokenLost(player.number()));
        }
    }

    public void revealLoyaltyCard(LoyaltyCard card) {
        record(new LoyaltyCardRevealed(player.number(), card));
    }

    public void revealMotiveCard(MotiveCard card) {
        record(new MotiveCardRevealed(player.number(), card));
    }

    public void discardLoyaltyCards(List<LoyaltyCard> cards) {
        if (!cards.isEmpty()) {
            record(new CardsLost(player.number(), PlayerPile.LOYALTY, cards));
            record(new CardsDiscarded(DeckId.LOYALTY, cards));
        }
    }

    public void giveLoyaltyCards(int recipientPlayerNumber, List<LoyaltyCard> cards) {
        record(new CardsLost(player.number(), PlayerPile.LOYALTY, cards));
        record(new CardsGained(recipientPlayerNumber, PlayerPile.LOYALTY, cards));
    }

    /**
     * Takes the character out of the game, e.g. after an execution. A viper he pilots goes back to the "Reserves".
     */
    public void removeCharacter() {
        val character = player.character();
        galacticaBoard().pilotedFighter(character).ifPresent(fighter -> ship(fighter.id()).returnToReserves());
        if (game.locate(character).isPresent()) {
            record(new CharacterRemovedFromBoards(character));
        }
        record(new CharacterRemoved(player.number(), character));
    }

    /**
     * Ends the Cylon Leader's Infiltration, for any reason (rule 6.2.3.5): he goes back to the "Resurrection Ship" and
     * discards the Quorum Cards given to him, without effect, and his Mutiny Cards. He keeps his miracle token (rule
     * 4.3.2).
     */
    public void endInfiltration() {
        endInfiltration(Location.RESURRECTION_SHIP);
    }

    /**
     * Ends the Infiltration at another Cylon location, as Aaron Doral's "Meticulous" lets him.
     */
    public void endInfiltration(Location destination) {
        record(new InfiltrationEnded(player.number()));
        sendTo(destination);
        titles().discardQuorumCardsGivenTo(player.number());
        discard(player.mutinyCards().cards().toArray(MutinyCard[]::new));
    }

    private void gain(PlayerPile pile, List<?> cards) {
        record(new CardsGained(player.number(), pile, cards));
    }
}
