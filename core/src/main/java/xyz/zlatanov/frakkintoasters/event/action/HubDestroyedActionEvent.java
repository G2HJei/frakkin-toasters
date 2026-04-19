package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.board.Location.CYLON_FLEET;

public record HubDestroyedActionEvent(int playerNumber, SkillCard discardCard1, SkillCard discardCard2,
                                      SkillCard discardCard3) implements ActionEvent {

    @Override
    public Followup apply(Game game) {
        val player = player(game);
        for (val card : List.of(discardCard1, discardCard2, discardCard3)) {
            player.skillCards().remove(card);
            game.decks().discard(card);
        }
        player.superCrisisCards().add(game.decks().superCrisis().draw());
        game.moveTo(CYLON_FLEET, playerCharacter(game));
        return Followup.NONE;
    }
}
