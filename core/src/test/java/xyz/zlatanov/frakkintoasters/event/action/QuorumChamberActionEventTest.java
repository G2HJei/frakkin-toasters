package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.DrawQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.state.Game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class QuorumChamberActionEventTest {

    @Test
    void shouldDrawQuorumCardAndFollowup() {
        val game = Game.builder().build();
        game.player(1).selectCharacter(LAURA_ROSLIN);
        game.president(LAURA_ROSLIN);

        val followup = new QuorumChamberActionEvent(1).execute(game);

        assertEquals(1, game.presidentHand().cards().size());
        assertEquals(expectedFollowup(), followup);
    }

    private Followup expectedFollowup() {
        return one(
                new DrawQuorumCardEvent(),
                new PlayQuorumCardEvent());
    }
}
