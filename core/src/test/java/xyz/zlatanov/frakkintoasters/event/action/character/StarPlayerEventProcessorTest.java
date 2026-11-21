package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ARMORY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAMUEL_T_ANDERS;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.STAR_PLAYER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.PILOTING;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class StarPlayerEventProcessorTest extends EventTestHarness<StarPlayerEvent> {

    private static final SkillCard ORDER   = new SkillCard(1, EXECUTIVE_ORDER);
    private static final SkillCard SCOUT   = new SkillCard(2, LAUNCH_SCOUT);
    private static final SkillCard EVASIVE = new SkillCard(3, EVASIVE_MANOEUVRES);
    private static final SkillCard FIRE    = new SkillCard(4, MAXIMUM_FIREPOWER);

    @BeforeEach
    void setUp() {
        player(1).character(SAMUEL_T_ANDERS);
        moveTo(ARMORY, SAMUEL_T_ANDERS);
        player(1).gainSkillCards(ORDER, SCOUT);
    }

    @Test
    void tradesCardsForAsManyOfOneTypeOfHisSkillSet() {
        pilotingDeck.nextCard(EVASIVE, FIRE);

        execute(new StarPlayerEvent(1, List.of(ORDER, SCOUT), PILOTING));

        assertSkillCards(1, EVASIVE, FIRE);
        assertTrue(game.turn().hasUsed(1, STAR_PLAYER));
    }

    @Test
    void notATypeOutsideHisSkillSet() {
        assertInvalid(new StarPlayerEvent(1, List.of(ORDER), POLITICS));
    }

    @Test
    void notCardsHeDoesntHold() {
        assertInvalid(new StarPlayerEvent(1, List.of(EVASIVE), PILOTING));
    }
}
