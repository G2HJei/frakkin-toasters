package xyz.zlatanov.frakkintoasters.event.turn;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.STIM_JUNKIE;

public class EndOfActionStepEventProcessor extends EventProcessor<EndOfActionStepEvent> {

    public static boolean actsThen(PlayerView player) {
        return player.hasAbility(STIM_JUNKIE);
    }

    // "Stim Junkie": still in the location or space area where her turn started, she goes to "Sickbay". In "Sickbay"
    // already nothing changes, and in the "Brig" she stays (rule 9.3.2).
    @Override
    public Followup process() {
        val current = game.currentPlayer();
        if (game.turn().endedEarly() || !current.hasAbility(STIM_JUNKIE)) {
            return NONE;
        }
        val location = game.locate(current.character());
        val start = game.turn().startLocation();
        if (location.isEmpty() || !location.equals(start) || location.get() == SICKBAY || location.get() == BRIG) {
            return NONE;
        }
        ops.player(current.number()).useAbility(STIM_JUNKIE);
        return single(new SendToSickbayEvent(current.number()));
    }
}
