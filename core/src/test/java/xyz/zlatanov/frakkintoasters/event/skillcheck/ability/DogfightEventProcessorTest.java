package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.DamageHumanFighterEvent;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.DOGFIGHT;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.MAJOR_VICTORY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheck.ADMIRALS_QUARTERS;

class DogfightEventProcessorTest extends EventTestHarness<DogfightEvent> {

    private static final SkillCard DOGFIGHT_0      = new SkillCard(0, DOGFIGHT);
    private static final SkillCard MAJOR_VICTORY_4 = new SkillCard(4, MAJOR_VICTORY);
    private static final int       RESERVES_VIPER  = 1;

    private SkillCheckHolder skillCheck;

    @BeforeEach
    void setUp() {
        skillCheck = game.startSkillCheck(ADMIRALS_QUARTERS);
        skillCheck.matchingPile().add(MAJOR_VICTORY_4);
        skillCheck.nonMatchingPile().add(DOGFIGHT_0);
    }

    @Test
    void shouldRemoveCardAndDamageViperInSpace() {
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);

        execute(new DogfightEvent(1, viper.id(), MAJOR_VICTORY_4));

        assertEquals(List.of(), skillCheck.matchingPile());
        assertEquals(List.of(MAJOR_VICTORY_4), leadershipDeck.discardedCards());
        assertFollowup(new DamageHumanFighterEvent(viper.id()));
    }

    @Test
    void shouldAllowRemovingAnotherDogfightCard() {
        skillCheck.nonMatchingPile().add(DOGFIGHT_0);

        execute(new DogfightEvent(1, RESERVES_VIPER, DOGFIGHT_0));

        assertEquals(List.of(DOGFIGHT_0), skillCheck.nonMatchingPile());
        assertFollowup(new DamageHumanFighterEvent(RESERVES_VIPER));
    }

    @Test
    void shouldAllowDecliningTheAbility() {
        execute(new DogfightEvent(1, null, null));

        assertEquals(List.of(MAJOR_VICTORY_4), skillCheck.matchingPile());
        assertEquals(List.of(DOGFIGHT_0), skillCheck.nonMatchingPile());
    }
}
