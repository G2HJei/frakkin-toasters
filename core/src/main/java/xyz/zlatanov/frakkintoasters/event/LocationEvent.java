package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.event.location.colonialone.PressRoomEventProcessor;
import xyz.zlatanov.frakkintoasters.event.location.colonialone.QuorumChamberEventProcessor;
import xyz.zlatanov.frakkintoasters.event.location.galactica.*;
import xyz.zlatanov.frakkintoasters.event.location.pegasus.EngineRoomEventProcessor;
import xyz.zlatanov.frakkintoasters.event.location.pegasus.PegasusCicEventProcessor;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

/**
 * The action of a location. The player has to be at the location, and {@link #canAct} has to allow the action.
 */
public interface LocationEvent extends ActionEvent {

    /**
     * @return the location whose action this is
     */
    Location location();

    /**
     * @return whether the player can take the action of his location at all, whatever he then chooses. The Action step
     * asks for a location action only then, so the decision always has a legal answer
     */
    static boolean canAct(GameView game, PlayerView player) {
        return game.locate(player.character()).filter(location -> canAct(game, player, location)).isPresent();
    }

    /**
     * @return whether the player can take the action of the location. Each location's own rules are in its processor
     */
    static boolean canAct(GameView game, PlayerView player, Location location) {
        // Characters may move into a damaged location, but may not use its action (rule 11.7.2.2).
        if (game.boards().galactica().isDamaged(location) || game.boards().pegasus().isDamaged(location)) {
            return false;
        }
        return switch (location) {
            case ADMIRALS_QUARTERS -> AdmiralsQuartersEventProcessor.canAct(game);
            case ARMORY -> ArmoryEventProcessor.canAct(game);
            case FTL_CONTROL -> FTLControlEventProcessor.canAct(game);
            case HANGAR_DECK -> HangarDeckEventProcessor.canAct(game, player);
            case RESEARCH_LAB -> ResearchLabEventProcessor.canAct(game);
            case WEAPONS_CONTROL -> WeaponsControlEventProcessor.canAct(game);
            case PRESS_ROOM -> PressRoomEventProcessor.canAct(game, player);
            case QUORUM_CHAMBER -> QuorumChamberEventProcessor.canAct(game, player);
            case PEGASUS_CIC -> PegasusCicEventProcessor.canAct(game);
            case ENGINE_ROOM -> EngineRoomEventProcessor.canAct(player);
            case COMMAND, COMMUNICATIONS, PRESIDENTS_OFFICE, MAIN_BATTERIES, CAPRICA, CYLON_FLEET, HUMAN_FLEET,
                 RESURRECTION_SHIP -> true;
            // No action. The "Brig", "Administration", "Airlock" and "Basestar Bridge" actions come with phase 9.
            case SICKBAY, BRIG, ADMINISTRATION, AIRLOCK, BASESTAR_BRIDGE -> false;
            case GALACTICA_SPACE_12_OCLOCK, GALACTICA_SPACE_2_OCLOCK, GALACTICA_SPACE_4_OCLOCK, GALACTICA_SPACE_6_OCLOCK,
                 GALACTICA_SPACE_8_OCLOCK, GALACTICA_SPACE_10_OCLOCK, CYLON_FLEET_SPACE_1, CYLON_FLEET_SPACE_2,
                 CYLON_FLEET_SPACE_3, CYLON_FLEET_SPACE_4, CYLON_FLEET_SPACE_5_6, CYLON_FLEET_SPACE_7_8 -> false;
        };
    }
}
