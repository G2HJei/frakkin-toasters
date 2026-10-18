package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class PersonalVicesEventProcessorTest extends EventTestHarness<PersonalVicesEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LOUANNE_KAT_KATRAINE);
        player(2).character(KARA_STARBUCK_THRACE);
        player(3).character(LEE_ADAMA);
    }

    @Test
    void shouldDrawTreachery() {
        val treachery1 = new SkillCard(1, DRADIS_CONTACT);
        val treachery2 = new SkillCard(2, BAIT);
        val treachery3 = new SkillCard(3, A_BETTER_MACHINE);
        treacheryDeck.nextCard(treachery1, treachery2, treachery3);

        execute(new PersonalVicesEvent());

        assertSkillCards(1, treachery1);
        assertSkillCards(2, treachery2);
        assertSkillCards(3, treachery3);
    }

    @Test
    void shouldDrawMutiny() {
        execute(new PersonalVicesEvent());
        assertEquals(1, player(1).mutinyCards().size());
    }

    @Test
    void shouldNotDrawMutiny() {
        revealCylon();
        execute(new PersonalVicesEvent());
        assertTrue(player(1).mutinyCards().isEmpty());
    }
}