package xyz.zlatanov.frakkintoasters.state.view;

public interface PegasusBoardView extends BattlestarBoardView {

    boolean engineRoomActivated();

    /**
     * @return whether Pegasus was destroyed, its four locations damaged at the same time (rule 11.7.2.4): its board is out
     * of the game
     */
    boolean destroyed();
}
