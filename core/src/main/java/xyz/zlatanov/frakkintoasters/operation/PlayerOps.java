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
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.Arrays;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.util.AllCardsProvider.genericDeck;

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

    public void drawSkillCards(SkillCardColor... colors) {
        drawSkillCards(Arrays.stream(colors).toList());
    }

    public void drawSkillCards(List<SkillCardColor> colors) {
        for (val color : colors) {
            game.decks()
                    .drawSkillCard(color)
                    .ifPresent(this::giveToPlayer);
        }
    }

    public CrisisCard drawCrisisCard() {
        return game.decks().crisis().draw();
    }

    public void drawSuperCrisisCard() {
        val card = game.decks().superCrisis().draw();
        player.superCrisisCards()
                .addOnTop(card);
    }

    public void drawMutinyCard() {
        val card = game.decks().mutiny().draw();
        player.mutinyCards()
                .addOnTop(card);
    }

    public void drawMotiveCards() {
        val motiveCards = genericDeck(MotiveCard.class).draw(2);
        player.motiveCards().addOnTop(motiveCards);
    }

    public DestinationCard drawDestinationCard() {
        return game.decks().destination().draw();
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

    private void giveToPlayer(SkillCard card) {
        player.gainSkillCards(card);
    }

    public void infiltrateGalactica(Location infiltrateLocation) {
        player.infiltrateGalactica();
        character(player.character()).moveTo(infiltrateLocation);
    }

    public void lookAtShip(Integer civId) {
        player.revealedCivilianShipIds()
                .add(civId);
    }
}
