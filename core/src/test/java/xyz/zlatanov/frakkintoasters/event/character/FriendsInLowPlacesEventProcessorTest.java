package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ADMINISTRATION;
import static xyz.zlatanov.frakkintoasters.state.character.Character.TOM_ZAREK;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.BRIG;

class FriendsInLowPlacesEventProcessorTest extends EventTestHarness<FriendsInLowPlacesEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(TOM_ZAREK);
        moveTo(ADMINISTRATION, TOM_ZAREK);
    }

    @Test
    void raisesOrLowersTheBrigByTwo() {
        ops.skillCheck().start(BRIG, 2);
        assertEquals(one(new FriendsInLowPlacesEvent(1, -2), new FriendsInLowPlacesEvent(1, 2), new NoOpEvent(1)),
                FriendsInLowPlacesEventProcessor.offer(game));

        execute(new FriendsInLowPlacesEvent(1, 2));

        assertEquals(2, game.activeSkillCheck().difficultyAdjustment());
    }

    @Test
    void onlyByTwo() {
        ops.skillCheck().start(BRIG, 2);

        assertInvalid(new FriendsInLowPlacesEvent(1, 3));
    }

    @Test
    void onlyTheBrig() {
        ops.skillCheck().start(ADMIRALS_QUARTERS, 2);

        assertEquals(NONE, FriendsInLowPlacesEventProcessor.offer(game));
        assertInvalid(new FriendsInLowPlacesEvent(1, -2));
    }
}
