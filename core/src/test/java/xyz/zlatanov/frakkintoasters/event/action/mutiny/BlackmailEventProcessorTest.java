package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.BLACKMAIL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class BlackmailEventProcessorTest extends EventTestHarness<BlackmailEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(KARA_STARBUCK_THRACE);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        president(WILLIAM_ADAMA);
        admiral(WILLIAM_ADAMA);
        cag(KARA_STARBUCK_THRACE);
        player(1).mutinyCards().addOnTop(BLACKMAIL);
    }

    @Test
    void takesThreeRandomCardsFromThePresident() {
        player(2).gainSkillCards(new SkillCard(1, CONSOLIDATE_POWER), new SkillCard(2, EXECUTIVE_ORDER),
                new SkillCard(3, REPAIR), new SkillCard(4, MAXIMUM_FIREPOWER));

        execute(new BlackmailEvent(1));

        assertEquals(3, player(1).skillCards().size());
        assertEquals(1, player(2).skillCards().size());
        assertFollowup(new DiscardPlayedMutinyCardEvent(BLACKMAIL));
    }

    @Test
    void notWhenThePresidentHoldsTwoOrFewer() {
        player(2).gainSkillCards(new SkillCard(1, CONSOLIDATE_POWER), new SkillCard(2, EXECUTIVE_ORDER));

        assertInvalid(new BlackmailEvent(1));
    }

    @Test
    void notByThePresident() {
        president(LAURA_ROSLIN);
        player(1).gainSkillCards(new SkillCard(1, CONSOLIDATE_POWER), new SkillCard(2, EXECUTIVE_ORDER),
                new SkillCard(3, REPAIR));

        assertInvalid(new BlackmailEvent(1));
    }
}
