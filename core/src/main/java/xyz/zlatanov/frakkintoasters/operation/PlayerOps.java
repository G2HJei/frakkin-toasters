package xyz.zlatanov.frakkintoasters.operation;

import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Player;
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

    private void giveToPlayer(SkillCard card) {
        ((Player) game.player(playerNumber))
                .gainSkillCards(card);
    }
}
