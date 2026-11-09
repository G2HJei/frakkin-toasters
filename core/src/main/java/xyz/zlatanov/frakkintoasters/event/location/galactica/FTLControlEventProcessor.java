package xyz.zlatanov.frakkintoasters.event.location.galactica;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.JumpingTheFleetEvent;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.state.track.JumpPreparation;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.track.JumpPreparation.MINUS_1;
import static xyz.zlatanov.frakkintoasters.state.track.JumpPreparation.MINUS_3;

public class FTLControlEventProcessor extends LocationActionProcessor<FTLControlEvent> {

    private static final Set<JumpPreparation> OUT_OF_THE_RED_ZONE = Set.of(MINUS_3, MINUS_1);

    public static boolean canAct(GameView game) {
        return OUT_OF_THE_RED_ZONE.contains(game.boards().galactica().jumpPreparation());
    }

    @Override
    public Followup process() {
        val roll = ops.rollDie();
        if (roll < 7) {
            val lostPop = galacticaBoard.jumpPreparation().lostPopulation();
            ops.galactica().decreasePopulation(lostPop);
        }
        return single(new JumpingTheFleetEvent());
    }
}
