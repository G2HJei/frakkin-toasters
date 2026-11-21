package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.CYLON_FLEET;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SIMON_ONEIL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EXECUTIVE_ORDER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.WATER_SABOTAGED;

class LogicBoundEventProcessorTest extends EventTestHarness<LogicBoundEvent> {

    private final SkillCard card = new SkillCard(3, EXECUTIVE_ORDER);

    @BeforeEach
    void setUp() {
        player(1).character(SIMON_ONEIL);
        moveTo(CYLON_FLEET, SIMON_ONEIL);
        player(1).gainSkillCards(card);
        ops.skillCheck().createDestinyDeck().start(WATER_SABOTAGED).playFromDestinyDeck(2);
        ops.skillCheck().playCards(1, card);
    }

    // Everyone sees the card he put in face up, as with "Investigative Committee".
    @Test
    void showsTheCard() {
        execute(new LogicBoundEvent(1, card));

        assertEquals(List.of(card), game.activeSkillCheck().shownCards(1));
    }

    @Test
    void onlyACardInTheCheck() {
        assertInvalid(new LogicBoundEvent(1, new SkillCard(5, EXECUTIVE_ORDER)));
    }

    @Test
    void onlySimon() {
        player(2).character(LAURA_ROSLIN);

        assertInvalid(new LogicBoundEvent(2, card));
    }
}
