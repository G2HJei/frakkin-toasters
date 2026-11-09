package xyz.zlatanov.frakkintoasters.event.turn;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_3;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class ReceiveStartingSkillsEventProcessorTest extends EventTestHarness<ReceiveStartingSkillsEvent> {

    // "Each player, except the starting player and Cylon Leaders, draws a total of three Skill Cards" (rule A3.9)
    @Test
    void everyPlayerButTheFirstDrawsThreeCardsClockwise() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(KARA_STARBUCK_THRACE);
        game.turn().currentPlayerNumber(2);

        execute(new ReceiveStartingSkillsEvent());

        assertFollowup(all(draw(3, DRAW_EXACTLY_3), draw(1, DRAW_EXACTLY_3)));
    }

    // "A Cylon Leader draws two Skill Cards (except Sharon "Athena" Agathon who starts the game Infiltrating and draws
    // three Skill Cards)." (rule A3.9)
    @Test
    void aCylonLeaderDrawsTwoCardsUnlessInfiltrating() {
        setUpGame(5);
        player(1).character(LAURA_ROSLIN);
        player(2).character(CAVIL);
        player(3).character(SHARON_ATHENA_AGATHON).infiltrateGalactica();
        player(4).character(WILLIAM_ADAMA);
        player(5).character(KARA_STARBUCK_THRACE);

        execute(new ReceiveStartingSkillsEvent());

        assertFollowup(all(draw(2, DRAW_EXACTLY_2), draw(3, DRAW_EXACTLY_3), draw(4, DRAW_EXACTLY_3),
                draw(5, DRAW_EXACTLY_3)));
    }

    private static PlayerDecisionEvent<DrawSkillCardsEvent> draw(int player, EventConstraint count) {
        return new PlayerDecisionEvent<>(player, DrawSkillCardsEvent.class, count);
    }
}
