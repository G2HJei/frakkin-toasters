package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import static xyz.zlatanov.frakkintoasters.state.character.Character.SHERMAN_DOC_COTTLE;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CALLANDRA_CALLY_TYROL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.REPAIR;

// Callandra "Cally" Tyrol plays them: "Chief" Galen Tyrol's "Maintenance Engineer" answers his "Repair".
class RepairEventProcessorTest extends EventTestHarness<RepairEvent> {

    private static final SkillCard REPAIR_1 = new SkillCard(1, REPAIR);

    @BeforeEach
    void setUp() {
        player(1).character(CALLANDRA_CALLY_TYROL);
        player(1).gainSkillCards(REPAIR_1);
    }

    // Its action can be taken again (rule 11.7.2.2).
    @Test
    void theLocationIsRepaired() {
        ops.sandbox().damage(COMMAND);
        moveTo(COMMAND, CALLANDRA_CALLY_TYROL);

        execute(new RepairEvent(1, REPAIR_1, List.of()));

        assertFalse(galacticaBoard.isDamaged(COMMAND));
        assertTrue(LocationEvent.canAct(game, player(1)));
    }

    @Test
    void aPegasusLocationIsRepairedToo() {
        ops.sandbox().damage(AIRLOCK);
        moveTo(AIRLOCK, CALLANDRA_CALLY_TYROL);

        execute(new RepairEvent(1, REPAIR_1, List.of()));

        assertFalse(pegasusBoard.isDamaged(AIRLOCK));
    }

    // The damaged vipers go back to the "Reserves". The Mark VIIs start damaged (rule 11.3.1).
    @Test
    void twoDamagedVipersAtTheHangarDeck() {
        moveTo(HANGAR_DECK, CALLANDRA_CALLY_TYROL);
        List<Integer> damaged = galacticaBoard.damagedShips().stream().map(Ship::id).limit(2).toList();

        execute(new RepairEvent(1, REPAIR_1, damaged));

        assertTrue(galacticaBoard.reserves().stream().map(Ship::id).toList().containsAll(damaged));
        assertEquals(2, galacticaBoard.damagedShips().size());
    }

    @Test
    void notAnUndamagedLocation() {
        moveTo(COMMAND, CALLANDRA_CALLY_TYROL);

        assertInvalid(new RepairEvent(1, REPAIR_1, List.of()));
    }

    @Test
    void vipersOnlyAtTheHangarDeck() {
        moveTo(COMMAND, CALLANDRA_CALLY_TYROL);

        assertInvalid(new RepairEvent(1, REPAIR_1, List.of(71)));
    }

    @Test
    void atMostTwoVipers() {
        moveTo(HANGAR_DECK, CALLANDRA_CALLY_TYROL);

        assertInvalid(new RepairEvent(1, REPAIR_1, List.of(71, 72, 73)));
    }

    @Test
    void onlyDamagedVipers() {
        moveTo(HANGAR_DECK, CALLANDRA_CALLY_TYROL);

        assertInvalid(new RepairEvent(1, REPAIR_1, List.of(1)));
    }

    @Test
    void notTheSameViperTwice() {
        moveTo(HANGAR_DECK, CALLANDRA_CALLY_TYROL);

        assertInvalid(new RepairEvent(1, REPAIR_1, List.of(71, 71)));
    }

    // "Specialized": Sherman "Doc" Cottle can't use the actions of Engineering cards.
    @Test
    void docCottleCantPlayIt() {
        setUpGame();
        player(1).character(SHERMAN_DOC_COTTLE);
        player(1).gainSkillCards(REPAIR_1);
        ops.sandbox().damage(COMMAND);
        moveTo(COMMAND, SHERMAN_DOC_COTTLE);

        assertInvalid(new RepairEvent(1, REPAIR_1, List.of()));
    }
}
