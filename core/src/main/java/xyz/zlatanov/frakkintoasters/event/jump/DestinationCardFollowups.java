package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.board.IncreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.event.jump.RiskShipsEvent.Risk;
import xyz.zlatanov.frakkintoasters.event.ship.DamageBattlestarEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyRaptorEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DrawAndDestroyCivilianShipsEvent;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceCivilianShipsInFrontEvent;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceRaidersBehindEvent;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceShipsEvent;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.optional;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FUEL;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;

/**
 * What a Destination Card does when the fleet jumps to it (rule 12.3), as printed on the card. The card text is in the
 * comments, and the images are in {@code materials/images}. The Admiral makes the choices.
 */
@NoArgsConstructor(access = PRIVATE)
public final class DestinationCardFollowups {

    public static Followup effects(GameView game, DestinationCard card) {
        val admiral = game.playerNumber(game.admiral());
        return switch (card) {
            // Lose 2 fuel. Then draw 1 civilian ship and destroy it (lose the resources on the back).
            case ASTEROID_FIELD -> all(lose(FUEL, 2), new DrawAndDestroyCivilianShipsEvent(1));
            // Lose 2 fuel.
            case BARREN_PLANET -> single(lose(FUEL, 2));
            // Lose 1 fuel. Then place 1 basestar and 3 raiders in front of Galactica and 3 civilian ships behind Galactica.
            case CYLON_AMBUSH -> all(lose(FUEL, 1), new PlaceShipsEvent(1, 3, 3));
            // Lose 1 fuel. The Admiral may risk 2 vipers to roll a die. If 6 or higher, gain 2 fuel. Otherwise, damage 2 vipers.
            case CYLON_REFINERY -> allOf(single(lose(FUEL, 1)), risk(game, admiral, Risk.CYLON_REFINERY));
            // Lose 1 fuel and 1 morale.
            case DEEP_SPACE -> all(lose(FUEL, 1), lose(MORALE, 1));
            // Lose 3 fuel.
            case DESOLATE_MOON -> single(lose(FUEL, 3));
            // Lose 1 fuel. The Admiral may risk 1 raptor to roll a die. If 3 or higher, gain 1 food. Otherwise, destroy 1 raptor.
            case ICY_MOON -> allOf(single(lose(FUEL, 1)), risk(game, admiral, Risk.ICY_MOON));
            // The Admiral may repair up to 3 vipers and 1 raptor. These ships may be damaged or even destroyed.
            case RAGNAR_ANCHORAGE -> single(new RagnarAnchorageEvent());
            // Lose 1 fuel and destroy 1 raptor.
            case REMOTE_PLANET -> all(lose(FUEL, 1), new DestroyRaptorEvent());
            // Lose 1 fuel. The Admiral may risk 1 raptor to roll a die. If 3 or higher, gain 2 fuel. Otherwise, destroy 1 raptor.
            case TYLIUM_PLANET -> allOf(single(lose(FUEL, 1)), risk(game, admiral, Risk.TYLIUM_PLANET));
            // Lose 1 fuel and gain 1 food.
            case ALGAE_PLANET -> all(lose(FUEL, 1), gain(FOOD, 1));
            // Lose 2 fuel and place 3 raiders behind Galactica.
            case CYLON_RAIDERS -> all(lose(FUEL, 2), new PlaceRaidersBehindEvent(3));
            // Lose 1 fuel. Then place 2 civilian ships behind Galactica and 1 basestar in front of Galactica. Damage the
            // basestar once.
            case DERELICT_BASESTAR -> all(lose(FUEL, 1), new DerelictBasestarEvent());
            // Lose 1 fuel and damage Galactica once.
            case DYING_STAR -> all(lose(FUEL, 1), new DamageBattlestarEvent());
            // The Admiral may destroy 1 viper to gain 1 fuel.
            case GAS_GIANT -> GasGiantEventProcessor.canDestroyAViper(game)
                    ? optional(admiral, new GasGiantEvent(admiral))
                    : NONE;
            // Lose 4 fuel. After the Reset Jump Preparation Track step of this jump, advance the Jump Preparation track by 2.
            case LIONS_HEAD_NEBULA -> single(lose(FUEL, 4));
            // Lose 1 fuel and 1 population.
            case RADIOACTIVE_CLOUD -> all(lose(FUEL, 1), lose(POPULATION, 1));
            // Lose 3 fuel and gain 1 population. The Admiral may choose to lose 1 morale to gain 1 fuel.
            case A_CIVILIAN_CONVOY -> all(
                    single(lose(FUEL, 3)),
                    single(gain(POPULATION, 1)),
                    Followup.one(all(lose(MORALE, 1), gain(FUEL, 1)), single(new NoOpEvent(admiral))));
            // Lose 1 fuel. Place 1 civilian ship in front of Galactica and 1 civilian ship behind Galactica.
            case BINARY_STAR -> all(lose(FUEL, 1), new PlaceCivilianShipsInFrontEvent(1), new PlaceShipsEvent(0, 0, 1));
            // The Admiral may look at the top 3 cards of the Crisis deck, then place them on the top or bottom of the deck
            // in any order.
            case GAS_CLOUD -> single(new GasCloudEvent(admiral));
            // Resolved by ResolveDestinationCardEventProcessor: it draws another card.
            case MISJUMP -> throw new FrakCallTheAdmiralException();
        };
    }

    /**
     * @return what follows the Reset Jump Preparation Track step: "Lion's Head Nebula" advances the track by 2
     */
    public static Followup afterReset(DestinationCard card) {
        return card == DestinationCard.LIONS_HEAD_NEBULA
                ? all(new IncreaseJumpPreparationEvent(), new IncreaseJumpPreparationEvent())
                : NONE;
    }

    // The Admiral may risk the ships only when there are enough of them in the "Reserves" (rule 13.5).
    private static Followup risk(GameView game, int admiral, Risk risk) {
        return RiskShipsEventProcessor.canRisk(game, risk) ? optional(admiral, new RiskShipsEvent(admiral, risk)) : NONE;
    }
}
