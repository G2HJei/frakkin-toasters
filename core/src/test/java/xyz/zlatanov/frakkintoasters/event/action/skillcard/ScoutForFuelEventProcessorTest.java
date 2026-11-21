package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.SCOUTING_FOR_FUEL;

class ScoutForFuelEventProcessorTest extends EventTestHarness<ScoutForFuelEvent> {

    private static final SkillCard SCOUT_FOR_FUEL_6 = new SkillCard(6, SCOUTING_FOR_FUEL);

    @BeforeEach
    void setUp() {
        player(1).character(WILLIAM_ADAMA);
        player(1).gainSkillCards(SCOUT_FOR_FUEL_6);
    }

    @Test
    void aRaptorIsRiskedOnARoll() {
        execute(new ScoutForFuelEvent(1, SCOUT_FOR_FUEL_6));

        assertFollowup(rollDie(new ScoutForFuelResultEvent()));
    }

    @Test
    void notWithoutARaptor() {
        while (galacticaBoard.removeFromReserves(Raptor.class).isPresent()) {
            // none left to risk
        }

        assertInvalid(new ScoutForFuelEvent(1, SCOUT_FOR_FUEL_6));
    }
}
