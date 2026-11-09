package xyz.zlatanov.frakkintoasters.event.constraint;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.event.player.LeaveBrigEvent;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_6_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;

class ConstrainedEventTest {

    @Test
    void theConstraintsReplaceTheOnesTheEventHad() {
        val draw = new DrawSkillCardsEvent(1, Map.of(POLITICS, 2), DRAW_EXACTLY_5);
        assertEquals(DRAW_EXACTLY_2, draw.withConstraints(List.of(DRAW_EXACTLY_2)).drawLimit());
        assertNull(draw.withConstraints(List.of()).drawLimit());

        val discard = new DiscardSkillCardsEvent(1, List.of(), DISCARD_EXACTLY_1);
        assertEquals(DISCARD_EXACTLY_4, discard.withConstraints(List.of(DISCARD_EXACTLY_4)).discardLimit());
        assertNull(discard.withConstraints(List.of()).discardLimit());

        val leave = new LeaveBrigEvent(1, COMMAND);
        assertEquals(DESTINATION_GALACTICA, leave.withConstraints(List.of(DESTINATION_GALACTICA)).destinationLimit());
        assertNull(new LeaveBrigEvent(1, COMMAND, DESTINATION_GALACTICA).withConstraints(List.of()).destinationLimit());
    }

    @Test
    void theRestOfTheEventStaysTheSame() {
        val draw = new DrawSkillCardsEvent(1, Map.of(POLITICS, 2));
        assertEquals(new DrawSkillCardsEvent(1, Map.of(POLITICS, 2), DRAW_EXACTLY_2),
                draw.withConstraints(List.of(DRAW_EXACTLY_2)));
    }

    @Test
    void aSingleConstraintComponentTakesOnlyOneConstraint() {
        val draw = new DrawSkillCardsEvent(1, Map.of(POLITICS, 2));
        assertThrows(FrakCallTheAdmiralException.class, () -> draw.withConstraints(List.of(DRAW_EXACTLY_1, DRAW_EXACTLY_2)));
    }

    @Test
    void aViperLaunchMapsItsConstraintOntoTheFlag() {
        val launch = new LaunchViperEvent(1, VIPER, GALACTICA_SPACE_6_OCLOCK, KARA_STARBUCK_THRACE, null, true);

        assertTrue(launch.withConstraints(List.of(MUST_LAUNCH_IN_VIPER)).mustLaunchInViper());
        assertFalse(launch.withConstraints(List.of()).mustLaunchInViper());
        assertThrows(FrakCallTheAdmiralException.class, () -> launch.withConstraints(List.of(DRAW_EXACTLY_2)));
    }
}
