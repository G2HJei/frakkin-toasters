package xyz.zlatanov.frakkintoasters.event.location.cylon;

import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;

import static xyz.zlatanov.frakkintoasters.state.board.Location.CAPRICA;

// Caprica (Cylon location, Daybreak), its first option: play one of your Super Crisis Cards. No Activate Cylon Ships
// step follows. The other option is CapricaDrawAndResolveCrisisCardsEvent.
public record CapricaPlaySuperCrisisCardEvent(int playerNumber, SuperCrisisCard superCrisisCard) implements LocationEvent {

    @Override
    public Location location() {
        return CAPRICA;
    }
}
