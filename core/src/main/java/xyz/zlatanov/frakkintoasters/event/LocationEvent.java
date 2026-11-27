package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.event.location.cylon.CylonFleetEventProcessor;
import xyz.zlatanov.frakkintoasters.event.location.cylon.ResurrectionShipEventProcessor;
import xyz.zlatanov.frakkintoasters.event.location.colonialone.PressRoomEventProcessor;
import xyz.zlatanov.frakkintoasters.event.location.colonialone.QuorumChamberEventProcessor;
import xyz.zlatanov.frakkintoasters.event.location.galactica.*;
import xyz.zlatanov.frakkintoasters.event.location.pegasus.AirlockEventProcessor;
import xyz.zlatanov.frakkintoasters.event.location.pegasus.EngineRoomEventProcessor;
import xyz.zlatanov.frakkintoasters.event.location.pegasus.PegasusCicEventProcessor;
import xyz.zlatanov.frakkintoasters.event.character.ActionCosts;
import xyz.zlatanov.frakkintoasters.event.character.CharacterBans;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

public interface LocationEvent extends ActionEvent {

    Location location();

    /**
     * The Action step asks for a location action only then, so the decision always has a legal answer.
     */
    static boolean canAct(GameView game, PlayerView player) {
        return game.locate(player.character()).filter(location -> canAct(game, player, location)).isPresent();
    }

    static boolean canAct(GameView game, PlayerView player, Location location) {
        // Characters may move into a damaged location, but may not use its action (rule 11.7.2.2).
        if (game.boards().galactica().isDamaged(location) || game.boards().pegasus().isDamaged(location)) {
            return false;
        }
        if (game.isDestroyed(location)) {
            return false; // nobody is left there after "Bomb on Colonial One", and nobody can come back (rule 9.4)
        }
        return canActIgnoringDamage(game, player, location);
    }

    /**
     * @return whether the player can take the action of the location by its own rules, damaged or not, e.g. through an
     * "Unorthodox Plan"
     */
    static boolean canActIgnoringDamage(GameView game, PlayerView player, Location location) {
        if (!CharacterBans.canActivate(game, player, location) || !ActionCosts.canPayLocationCost(game, player, location)) {
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
            case AIRLOCK -> AirlockEventProcessor.canAct(game);
            case CYLON_FLEET -> CylonFleetEventProcessor.canAct(game);
            case RESURRECTION_SHIP -> ResurrectionShipEventProcessor.canAct(game);
            case COMMAND, COMMUNICATIONS, PRESIDENTS_OFFICE, MAIN_BATTERIES, CAPRICA, HUMAN_FLEET,
                 BASESTAR_BRIDGE, BRIG, ADMINISTRATION -> true;
            // No action.
            case SICKBAY -> false;
            case GALACTICA_SPACE_1_OCLOCK, GALACTICA_SPACE_3_OCLOCK, GALACTICA_SPACE_5_OCLOCK, GALACTICA_SPACE_7_OCLOCK,
                 GALACTICA_SPACE_9_OCLOCK, GALACTICA_SPACE_11_OCLOCK, CYLON_FLEET_SPACE_1, CYLON_FLEET_SPACE_2,
                 CYLON_FLEET_SPACE_3, CYLON_FLEET_SPACE_4, CYLON_FLEET_SPACE_5_6, CYLON_FLEET_SPACE_7_8 -> false;
        };
    }
}
