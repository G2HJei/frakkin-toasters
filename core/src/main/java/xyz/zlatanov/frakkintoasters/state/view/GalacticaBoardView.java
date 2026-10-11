package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.Centurion;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.track.BoardingParty;
import xyz.zlatanov.frakkintoasters.state.track.JumpPreparation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_6_OCLOCK;

public interface GalacticaBoardView extends BoardView, SpaceLocationsBoardView {

    List<Location> VIPER_LAUNCH_SPACES = List.of(GALACTICA_SPACE_4_OCLOCK, GALACTICA_SPACE_6_OCLOCK);

    int fuel();

    int food();

    int morale();

    int population();

    JumpPreparation jumpPreparation();

    Set<Ship> reserves();

    Map<Centurion, BoardingParty> boardingPartyTrack();
}
