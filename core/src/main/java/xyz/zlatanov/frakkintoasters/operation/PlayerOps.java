package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Player;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.Arrays;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.*;


public class PlayerOps extends GameOps {

    private final Player player;

    PlayerOps(Game game, int playerNumber) {
        super(game);
        this.player = game.player(playerNumber);
    }

    public void selectCharacter(Character selectedCharacter) {
        player.character(selectedCharacter);
    }

    public void drawLoyaltyCards(int cardsToDraw) {
        val loyaltyCards = game.decks().loyalty().draw(cardsToDraw);
        player.loyaltyCards().addOnTop(loyaltyCards);
    }

    public void moveTo(Location location) { //todo move to brig makes Admiral lose its title
        assert !location.isSpaceLocation();
        val character = player.character();
        game.boards().all().forEach(b -> b.remove(character));
        game.boards().all().stream()
                .filter(b -> b.locations().contains(location))
                .findFirst()
                .orElseThrow(FrakCallTheAdmiralException::new)
                .place(location, character);
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
            SkillCard draw = draw(of(color));
            player.gainSkillCards(draw);
        }
    }

    public CrisisCard drawCrisisCard() {
        return draw(CRISIS);
    }

    public PlayerOps drawSuperCrisisCard() {
        SuperCrisisCard card = draw(SUPER_CRISIS);
        player.superCrisisCards()
                .addOnTop(card);
        return this;
    }

    public void drawMutinyCard() {
        MutinyCard card = draw(MUTINY);
        player.mutinyCards().addOnTop(card);
        //todo if 2nd -> discard one and move to brig. Handle followup in engine not in each mutiny drawing event
    }

    public void drawMotiveCards() {
        List<MotiveCard> motiveCards = draw(MOTIVE, 2);
        player.motiveCards().addOnTop(motiveCards);
    }

    public DestinationCard drawDestinationCard() {
        return draw(DESTINATION);
    }

    public void placeCrisisCardOnTop(CrisisCard card) {
        game.decks().crisis().addOnTop(card);
    }

    public void placeCrisisCardOnBottom(CrisisCard card) {
        game.decks().crisis().addToBottom(card);
    }

    public void placeDestinationCardOnTop(DestinationCard card) {
        game.decks().destination().addOnTop(card);
    }

    public void placeDestinationCardOnBottom(DestinationCard card) {
        game.decks().destination().addToBottom(card);
    }

    public PlayerOps discard(SkillCard... cards) {
        player.skillCards()
                .remove(cards);
        game.decks().discard(cards);
        return this;
    }

    public void discard(MutinyCard... cards) {
        player.mutinyCards()
                .remove(cards);
        game.decks().discard(Arrays.stream(cards).toList());
    }

    public void infiltrateGalactica(Location infiltrateLocation) {
        player.infiltrateGalactica();
        moveTo(infiltrateLocation);
    }

    public void lookAtShip(Integer civId) {
        player.revealedCivilianShipIds()
                .add(civId);
    }

    public void pilot(int humanFighterId) {
        val character = player.character();
        galacticaBoard().remove(character);
        galacticaBoard()
                .shipInSpace(humanFighterId, HumanFighter.class)
                .pilot(character);
    }

    public void receiveSkillCard(SkillCard card) {
        player.gainSkillCards(card);
    }

    public void gainMiracleToken() {
        player.gainMiracleToken();
    }
}
