package xyz.zlatanov.frakkintoasters.event.location.galactica;

import static xyz.zlatanov.frakkintoasters.state.character.Character.TOM_ZAREK;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.FollowupUtils;
import xyz.zlatanov.frakkintoasters.event.character.FriendsInLowPlacesEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.GAIUS_BALTAR;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.BRIG;

class BrigEventProcessorTest extends EventTestHarness<BrigEvent> {

    @BeforeEach
    void setUpInTheBrig() {
        player(1).character(GAIUS_BALTAR);
        moveTo(Location.BRIG, GAIUS_BALTAR);
    }

    // "Action: Pass this skill check to move to any location."
    @Test
    void shouldStartTheBrigSkillCheckForThePlayer() {
        execute(new BrigEvent(1));

        assertSkillCheckTriggered(BRIG);
        assertEquals(1, game.activeSkillCheck().targetPlayerNumber());
    }

    @Test
    void shouldNotAllowOutOfTheBrig() {
        moveTo(RESEARCH_LAB, GAIUS_BALTAR);
        assertInvalid(new BrigEvent(1));
    }

    // "Friends in Low Places": Tom Zarek is asked as the location is activated.
    @Test
    void friendsInLowPlacesIsOffered() {
        player(2).character(TOM_ZAREK);
        moveTo(Location.ADMINISTRATION, TOM_ZAREK);

        execute(new BrigEvent(1));

        assertFollowup(allOf(one(new FriendsInLowPlacesEvent(2, -2), new FriendsInLowPlacesEvent(2, 2), new NoOpEvent(2)),
                FollowupUtils.beforeSkillCheckFollowups(game)));
    }
}
