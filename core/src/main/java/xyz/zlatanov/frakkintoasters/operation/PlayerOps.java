package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsAddedOnTop;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsAddedToBottom;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.ShipFact.PilotChanged;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Player;
import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.Arrays;
import java.util.List;


public class PlayerOps extends GameOps {

    private final Player player;

    PlayerOps(Game game, FactRecorder recorder, int playerNumber) {
        super(game, recorder);
        this.player = game.player(playerNumber);
    }

    public void selectCharacter(Character selectedCharacter) {
        record(new CharacterSelected(player.number(), selectedCharacter));
    }

    public void drawLoyaltyCards(int cardsToDraw) {
        val loyaltyCards = draw(DeckId.LOYALTY, cardsToDraw);
        gain(PlayerPile.LOYALTY, loyaltyCards);
    }

    public void moveTo(Location location) { //todo move to brig makes Admiral lose its title
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

    public void drawSkillCards(List<SkillCardColor> colors) {
        for (val color : colors) {
            SkillCard card = draw(DeckId.of(color));
            gain(PlayerPile.SKILL, List.of(card));
        }
    }

    public CrisisCard drawCrisisCard() {
        return draw(DeckId.CRISIS);
    }

    public PlayerOps drawSuperCrisisCard() {
        val card = draw(DeckId.SUPER_CRISIS);
        gain(PlayerPile.SUPER_CRISIS, List.of(card));
        return this;
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
        record(new CardsLost(player.number(), PlayerPile.SKILL, List.of(cards)));
        Arrays.stream(cards).forEach(this::discardToDeck);
        return this;
    }

    public void discard(MutinyCard... cards) {
        record(new CardsLost(player.number(), PlayerPile.MUTINY, List.of(cards)));
        Arrays.stream(cards).forEach(this::discardToDeck);
    }

    public void infiltrateGalactica(Location infiltrateLocation) {
        record(new InfiltrationStarted(player.number()));
        moveTo(infiltrateLocation);
    }

    public void lookAtShip(Integer civId) {
        record(new CivilianShipRevealed(player.number(), civId));
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

    public void gainMiracleToken() {
        record(new MiracleTokenGained(player.number()));
    }

    private void gain(PlayerPile pile, List<?> cards) {
        record(new CardsGained(player.number(), pile, cards));
    }
}
