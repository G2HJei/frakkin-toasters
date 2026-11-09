package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;

import static xyz.zlatanov.frakkintoasters.state.board.Location.CAPRICA;

// Caprica (Cylon location, Daybreak)
// Action: Play 1 of your Super Crisis Cards or draw 2 Crisis Cards, choose 1 to resolve, and place the other on the
// bottom of the deck.*
// *No "Activate Cylon Ships" step.
public record CapricaPlaySuperCrisisCardEvent(int playerNumber,
                                              SuperCrisisCard superCrisisCard) implements ActionEvent {

    //todo follow up with chosen card's specific event

    @Override
    public Location location() {
        return CAPRICA;
    }
}
