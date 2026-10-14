package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.Arrays;

public class PlayerOps extends AbstractOps {

    private final int playerNumber;

    PlayerOps(Game game, int playerNumber) {
        super(game);
        this.playerNumber = playerNumber;
    }

    public void drawSkillCard(SkillCardColor skillCardColor) { //todo handle multiple cards?
        game.decks()
                .drawSkillCard(skillCardColor)
                .ifPresent(this::giveToPlayer);
    }

    public CrisisCard drawCrisisCard() {
        return game.decks().crisis().draw();
    }

    public PlayerOps drawSuperCrisisCard() {
        val card = game.decks().superCrisis().draw();
        game.player(playerNumber)
                .superCrisisCards()
                .addOnTop(card);
        return this;
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
        game.player(playerNumber)
                .skillCards()
                .remove(cards);
        game.decks().discard(cards);
        return this;
    }

    public void discard(MutinyCard... cards) {
        game.player(playerNumber)
                .mutinyCards()
                .remove(cards);
        game.decks().discard(Arrays.stream(cards).toList());
    }

    private void giveToPlayer(SkillCard card) {
        game.player(playerNumber)
                .gainSkillCards(card);
    }
}
