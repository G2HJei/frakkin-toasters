package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.ASSUME_COMMAND;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class AssumeCommandEventProcessorTest extends EventTestHarness<AssumeCommandEvent> {

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
        president(LAURA_ROSLIN);
        admiral(WILLIAM_ADAMA);
        cag(KARA_STARBUCK_THRACE);
        player(1).mutinyCards().addOnTop(ASSUME_COMMAND);
    }

    @Test
    void fiveCardsMakeThePlayerAdmiral() {
        player(1).gainSkillCards(five.toArray(SkillCard[]::new));

        execute(new AssumeCommandEvent(1, five));

        assertFollowup(all(new DiscardSkillCardsEvent(1, five), new MakeAdmiralEvent(LAURA_ROSLIN),
                new DiscardPlayedMutinyCardEvent(ASSUME_COMMAND)));
    }

    @Test
    void notWithFewerThanFiveCards() {
        player(1).gainSkillCards(five.subList(0, 4).toArray(SkillCard[]::new));

        assertInvalid(new AssumeCommandEvent(1, five.subList(0, 4)));
    }

    @Test
    void notByTheAdmiral() {
        admiral(LAURA_ROSLIN);
        player(1).gainSkillCards(five.toArray(SkillCard[]::new));

        assertInvalid(new AssumeCommandEvent(1, five));
    }

    @Test
    void notFromTheBrig() {
        moveTo(BRIG, LAURA_ROSLIN);
        player(1).gainSkillCards(five.toArray(SkillCard[]::new));

        assertInvalid(new AssumeCommandEvent(1, five));
    }

    @Test
    void notWithCardsHeDoesNotHold() {
        player(1).gainSkillCards(five.subList(0, 4).toArray(SkillCard[]::new));
        player(1).gainSkillCards(new SkillCard(3, REPAIR));

        assertInvalid(new AssumeCommandEvent(1, five));
    }
}
