package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.PRESIDENTS_OFFICE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;

class PresidentsOfficeEventProcessorTest extends EventTestHarness<PresidentsOfficeEvent> {

    @BeforeEach
    void setUpAtLocation() {
        player(1).character(LAURA_ROSLIN);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
    }

    @Test
    void shouldDraw2PoliticsCards() {
        execute(new PresidentsOfficeEvent(1));
        assertHas2PoliticsCards();
    }

    private void assertHas2PoliticsCards() {
        val colorOfCards = player(1)
                .skillCards().cards()
                .stream()
                .map(c -> c.type().color())
                .toList();
        assertEquals(List.of(POLITICS, POLITICS), colorOfCards);
    }

    @Test
    void shouldDrawOnlyThePoliticsCardsLeft() {
        politicsDeck.clear();
        game.decks().discard(new SkillCard(1, CONSOLIDATE_POWER));

        execute(new PresidentsOfficeEvent(1));

        assertSkillCards(1, new SkillCard(1, CONSOLIDATE_POWER));
    }
}
