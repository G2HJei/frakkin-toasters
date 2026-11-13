package xyz.zlatanov.frakkintoasters.event.board;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class AdvancePursuitTrackEventProcessor extends EventProcessor<AdvancePursuitTrackEvent> {
    @Override
    public Followup process() {
        val pursuit = ops.cylonFleet().advancePursuit();
        // Only the civilian ships that are left, so each decision has an answer.
        val civilianShipsLeft = game.decks().civilianShipsLeft();
        switch (pursuit) {
            case ONE_CIVILIAN_SHIP:
                return civilianShipsLeft > 0 ? single(decisionEvent()) : Followup.NONE;
            case TWO_CIVILIAN_SHIPS:
                return civilianShipsLeft > 1 ? all(decisionEvent(), decisionEvent())
                        : civilianShipsLeft > 0 ? single(decisionEvent()) : Followup.NONE;
            case AUTO_ATTACK:
                transferAllShipsToMainBoard();
            default:
                return Followup.NONE;
        }
    }

    private PlayerDecisionEvent<PlaceCivilianShipEvent> decisionEvent() {
        val cagPlayerNumber = game.players()
                .stream()
                .filter(p -> p.character() == game.cag())
                .findFirst()
                .map(PlayerView::number)
                .orElseThrow(FrakCallTheAdmiralException::new);
        return new PlayerDecisionEvent<>(cagPlayerNumber, PlaceCivilianShipEvent.class);
    }

    private void transferAllShipsToMainBoard() {
        ops.cylonFleet().resetPursuit();
        ops.cylonFleet().moveShipsToGalactica();
    }
}
