package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.IMPEACHMENT;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class ImpeachmentEventProcessorTest extends EventTestHarness<ImpeachmentEvent> {

    private final List<SkillCard> five = List.of(new SkillCard(1, CONSOLIDATE_POWER), new SkillCard(2, CONSOLIDATE_POWER),
            new SkillCard(1, EXECUTIVE_ORDER), new SkillCard(2, EXECUTIVE_ORDER), new SkillCard(1, REPAIR));

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
        player(1).mutinyCards().addOnTop(IMPEACHMENT);
    }

    @Test
    void fiveCardsMakeThePlayerPresidentEvenFromTheBrig() {
        moveTo(BRIG, LAURA_ROSLIN);
        player(1).gainSkillCards(five.toArray(SkillCard[]::new));

        execute(new ImpeachmentEvent(1, five));

        assertFollowup(all(new DiscardSkillCardsEvent(1, five), new MakePresidentEvent(LAURA_ROSLIN),
                new DiscardPlayedMutinyCardEvent(IMPEACHMENT)));
    }

    @Test
    void notWithFewerThanFiveCards() {
        player(1).gainSkillCards(five.subList(0, 4).toArray(SkillCard[]::new));

        assertInvalid(new ImpeachmentEvent(1, five.subList(0, 4)));
    }

    @Test
    void notByThePresident() {
        president(LAURA_ROSLIN);
        player(1).gainSkillCards(five.toArray(SkillCard[]::new));

        assertInvalid(new ImpeachmentEvent(1, five));
    }
}
