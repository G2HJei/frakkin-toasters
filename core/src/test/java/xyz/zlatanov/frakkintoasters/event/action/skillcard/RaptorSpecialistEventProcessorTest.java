package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.action.skillcard.RaptorSpecialistEvent.Choice.BUILD_ASSAULT_RAPTOR;
import static xyz.zlatanov.frakkintoasters.event.action.skillcard.RaptorSpecialistEvent.Choice.RETURN_RAPTOR;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CHIEF_GALEN_TYROL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.RAPTOR_SPECIALIST;

class RaptorSpecialistEventProcessorTest extends EventTestHarness<RaptorSpecialistEvent> {

    private static final SkillCard RAPTOR_SPECIALIST_3 = new SkillCard(3, RAPTOR_SPECIALIST);

    @BeforeEach
    void setUp() {
        player(1).character(CHIEF_GALEN_TYROL);
        player(1).gainSkillCards(RAPTOR_SPECIALIST_3);
    }

    // The raptor destroyed comes from the "Reserves", and the assault raptor gained goes there (rule 11.3.2.1).
    @Test
    void aRaptorBecomesAnAssaultRaptor() {
        execute(new RaptorSpecialistEvent(1, RAPTOR_SPECIALIST_3, BUILD_ASSAULT_RAPTOR));

        assertEquals(List.of(12, 13, 14), idsInReserves(Raptor.class));
        assertEquals(List.of(21, 22), idsInReserves(AssaultRaptor.class));
        assertEquals(List.of(23, 24), galacticaBoard.supply().stream().map(Ship::id).toList());
        assertTrue(game.removedComponents().items().contains(new Raptor(11)));
    }

    @Test
    void aDestroyedRaptorReturns() {
        ops.ship(11).destroy();

        execute(new RaptorSpecialistEvent(1, RAPTOR_SPECIALIST_3, RETURN_RAPTOR));

        assertEquals(List.of(11, 12, 13, 14), idsInReserves(Raptor.class));
    }

    @Test
    void nothingToReturn() {
        assertInvalid(new RaptorSpecialistEvent(1, RAPTOR_SPECIALIST_3, RETURN_RAPTOR));
    }

    @Test
    void noAssaultRaptorWithoutARaptorInTheReserves() {
        List.of(11, 12, 13, 14).forEach(id -> ops.ship(id).destroy());

        assertInvalid(new RaptorSpecialistEvent(1, RAPTOR_SPECIALIST_3, BUILD_ASSAULT_RAPTOR));
    }

    // There are 4 assault raptors (A2.4.4).
    @Test
    void noAssaultRaptorLeftToBuild() {
        ops.galactica().buildAssaultRaptor();
        ops.galactica().buildAssaultRaptor();
        ops.galactica().buildAssaultRaptor();

        assertInvalid(new RaptorSpecialistEvent(1, RAPTOR_SPECIALIST_3, BUILD_ASSAULT_RAPTOR));
    }

    @Test
    void theChoiceIsNamed() {
        assertInvalid(new RaptorSpecialistEvent(1, RAPTOR_SPECIALIST_3, null));
    }

    private List<Integer> idsInReserves(Class<? extends Ship> type) {
        return galacticaBoard.reserves().stream().filter(type::isInstance).map(Ship::id).toList();
    }
}
