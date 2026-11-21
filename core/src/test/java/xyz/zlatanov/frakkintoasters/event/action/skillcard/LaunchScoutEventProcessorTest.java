package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.LAUNCH_SCOUT;

class LaunchScoutEventProcessorTest extends EventTestHarness<LaunchScoutEvent> {

    private static final SkillCard LAUNCH_SCOUT_1 = new SkillCard(1, LAUNCH_SCOUT);

    @BeforeEach
    void setUp() {
        player(1).character(WILLIAM_ADAMA);
        player(1).gainSkillCards(LAUNCH_SCOUT_1);
    }

    @Test
    void aRaptorIsRiskedOnARoll() {
        execute(new LaunchScoutEvent(1, LAUNCH_SCOUT_1));

        assertFollowup(rollDie(new LaunchScoutResultEvent(1)));
    }

    // A raptor can be risked only when one is in the "Reserves" (rule 13.5).
    @Test
    void notWithoutARaptor() {
        while (galacticaBoard.removeFromReserves(Raptor.class).isPresent()) {
            // the assault raptor stays: it is no raptor
        }

        assertInvalid(new LaunchScoutEvent(1, LAUNCH_SCOUT_1));
    }
}
