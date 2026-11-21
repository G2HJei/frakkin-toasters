package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HUMAN_FLEET;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEOBEN_CONOY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class GlimpseTheFaceOfGodPlaceEventProcessorTest extends EventTestHarness<GlimpseTheFaceOfGodPlaceEvent> {

    private static final SkillCard ORDER   = new SkillCard(1, EXECUTIVE_ORDER);
    private static final SkillCard SCOUT   = new SkillCard(2, LAUNCH_SCOUT);
    private static final SkillCard REPAIRS = new SkillCard(3, REPAIR);

    @BeforeEach
    void setUp() {
        player(1).character(LEOBEN_CONOY);
        moveTo(HUMAN_FLEET, LEOBEN_CONOY);
        player(1).gainSkillCards(ORDER, SCOUT, REPAIRS);
    }

    // The first card he names ends on top.
    @Test
    void putsTwoCardsOnTopOfTheDestinyDeck() {
        execute(new GlimpseTheFaceOfGodPlaceEvent(1, List.of(REPAIRS, ORDER)));

        assertSkillCards(1, SCOUT);
        assertEquals(List.of(REPAIRS, ORDER), game.decks().destiny().cards().subList(0, 2));
    }

    @Test
    void notOneCard() {
        assertInvalid(new GlimpseTheFaceOfGodPlaceEvent(1, List.of(ORDER)));
    }

    @Test
    void notACardHeDoesntHold() {
        assertInvalid(new GlimpseTheFaceOfGodPlaceEvent(1, List.of(ORDER, new SkillCard(4, REPAIR))));
    }

    @Test
    void notWithoutCards() {
        assertInvalid(new GlimpseTheFaceOfGodPlaceEvent(1, null));
    }
}
