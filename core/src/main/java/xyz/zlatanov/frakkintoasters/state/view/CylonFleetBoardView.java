package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.track.Pursuit;

public interface CylonFleetBoardView extends BoardView, SpaceLocationsBoardView {

    Pursuit pursuitTrack();
}
