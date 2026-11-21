package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.SkillCheckFact.CardsLookedAt;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.PROBATION;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CALCULATIONS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EVASIVE_MANOEUVRES;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class UseProbationEventProcessorTest extends EventTestHarness<UseProbationEvent> {

    private static final SkillCard CALCULATIONS_1 = new SkillCard(1, CALCULATIONS);
    private static final SkillCard EVASIVE_2      = new SkillCard(2, EVASIVE_MANOEUVRES);

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        president(LAURA_ROSLIN);
        player(2).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        ops.titles().putInPlay(PROBATION, 2);
        ops.skillCheck().createDestinyDeck().start(ADMIRALS_QUARTERS).playFromDestinyDeck(2);
        player(2).gainSkillCards(CALCULATIONS_1, EVASIVE_2);
        ops.skillCheck().playCards(2, CALCULATIONS_1, EVASIVE_2);
    }

    // The holder's cards are the last ones put in, on top of the pile. Only the President learns them.
    @Test
    void thePresidentLooksAtTheHoldersCards() {
        execute(new UseProbationEvent(1, 2, 2));

        val looked = journal.facts().stream().filter(CardsLookedAt.class::isInstance).map(CardsLookedAt.class::cast).toList();
        assertEquals(1, looked.size());
        assertEquals(1, looked.getFirst().playerNumber());
        assertEquals(2, looked.getFirst().targetPlayerNumber());
        assertEquals(List.of(CALCULATIONS_1, EVASIVE_2), looked.getFirst().cards().stream().sorted(
                java.util.Comparator.comparing(SkillCard::value)).toList());
        assertEquals(List.of(), game.quorumCardsInPlay());
    }

    @Test
    void onlyThePresident() {
        assertInvalid(new UseProbationEvent(2, 2, 2));
    }
}
