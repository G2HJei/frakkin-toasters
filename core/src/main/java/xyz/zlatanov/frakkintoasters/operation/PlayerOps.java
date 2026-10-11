package xyz.zlatanov.frakkintoasters.operation;

import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Player;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

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

    private void giveToPlayer(SkillCard card) {
        ((Player) game.player(playerNumber))
                .gainSkillCards(card);
    }
}
