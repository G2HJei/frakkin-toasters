package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.board.Location.CYLON_FLEET;
import static xyz.zlatanov.frakkintoasters.state.board.Location.PRESIDENTS_OFFICE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class ActionCostsTest extends TestHarness {

    // "Terminal Illness": 2 cards before any location, and she can't activate one without them.
    @Test
    void roslinPays2ForAnyLocation() {
        player(1).character(LAURA_ROSLIN);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);

        assertEquals(2, ActionCosts.locationCost(player(1), COMMAND));
        assertFalse(LocationEvent.canAct(game, player(1)));

        player(1).gainSkillCards(new SkillCard(1, LAUNCH_SCOUT), new SkillCard(2, LAUNCH_SCOUT));
        assertTrue(LocationEvent.canAct(game, player(1)));
    }

    // "Conflicted Loyalties": 1 card for the "Cylon Fleet" only.
    @Test
    void capricaSixPays1ForTheCylonFleet() {
        player(1).character(CAPRICA_SIX);

        assertEquals(1, ActionCosts.locationCost(player(1), CYLON_FLEET));
        assertEquals(0, ActionCosts.locationCost(player(1), COMMAND));
    }

    // "Reluctant": 1 more card for an "Action:", none for a "Movement:"; the card he plays isn't one he can discard.
    @Test
    void hoshiPays1ForASkillCardAction() {
        player(1).character(LOUIS_HOSHI);
        val played = new SkillCard(1, CONSOLIDATE_POWER);
        val other = new SkillCard(2, LAUNCH_SCOUT);

        assertEquals(1, ActionCosts.skillCardCost(player(1), CONSOLIDATE_POWER));
        assertEquals(0, ActionCosts.skillCardCost(player(1), RUN_INTERFERENCE));

        player(1).gainSkillCards(played);
        assertFalse(ActionCosts.canPaySkillCardCost(game, player(1), CONSOLIDATE_POWER));

        player(1).gainSkillCards(other, played);
        assertTrue(ActionCosts.canPaySkillCardCost(game, player(1), CONSOLIDATE_POWER));
        assertEquals(one(new DiscardSkillCardsEvent(1, List.of(other)), new DiscardSkillCardsEvent(1, List.of(played))),
                ActionCosts.skillCardDiscard(player(1), played));
    }

    @Test
    void othersPayNothing() {
        player(1).character(SAUL_TIGH);

        assertEquals(0, ActionCosts.locationCost(player(1), CYLON_FLEET));
        assertEquals(0, ActionCosts.skillCardCost(player(1), CONSOLIDATE_POWER));
    }
}
