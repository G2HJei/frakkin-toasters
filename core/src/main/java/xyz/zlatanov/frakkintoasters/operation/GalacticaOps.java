package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.FOOD;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.FUEL;

public class GalacticaOps extends BattlestarOps {

    GalacticaOps(Game game) {
        super(game);
    }

    // todo certain effects destroy a random civ ship ( Misjump destination / Helena Cain miracle ability ). To handle this outside of ShipOps, probably GalacticaOps

    public void damage() {
        GalacticaDamage galacticaDamage = draw(DeckId.GALACTICA_DAMAGE);
        if (FUEL == galacticaDamage) {
            galacticaBoard().decreaseFuel(1);
            game.decks().galacticaDamage().addOnTop(FUEL);
            shuffle(DeckId.GALACTICA_DAMAGE);
        } else if (FOOD == galacticaDamage) {
            galacticaBoard().decreaseFood(1);
            game.decks().galacticaDamage().addOnTop(FOOD);
            shuffle(DeckId.GALACTICA_DAMAGE);
        } else {
            damageLocation(Location.valueOf(galacticaDamage.name()));
        }
    }

    /**
     * Todo phase 3: still mutates the boards without facts.
     */
    public void setUp() {
        val raiders = IntStream.range(0, 4).mapToObj(i -> game.cylonShips().raider()).map(Optional::orElseThrow).map(Ship.class::cast).toList();
        CivilianShip civ1 = draw(DeckId.CIVILIAN_SHIPS);
        CivilianShip civ2 = draw(DeckId.CIVILIAN_SHIPS);
        galacticaBoard()
                .place(GALACTICA_SPACE_4_OCLOCK, galacticaBoard().removeFromReserves(Viper.class).orElseThrow()) // reserves are never empty during setup
                .place(GALACTICA_SPACE_6_OCLOCK, galacticaBoard().removeFromReserves(Viper.class).orElseThrow())
                .place(GALACTICA_SPACE_2_OCLOCK, List.of(civ1, civ2))
                .place(GALACTICA_SPACE_8_OCLOCK, game.cylonShips().basestar().orElseThrow())
                .place(GALACTICA_SPACE_8_OCLOCK, raiders);
    }

    public void decreasePopulation(int populationLost) {
        galacticaBoard().decreasePopulation(populationLost);
    }

    public void decreaseMorale(int moraleLost) {
        galacticaBoard().decreaseMorale(moraleLost);
    }

    public void advanceBoardingParty() {
        galacticaBoard().advanceBoardingParty();
    }

    public void destroyCenturion(int centurionId) {
        val centurion = galacticaBoard().destroyCenturion(centurionId);
        game.cylonShips().returnedCenturion(centurion);
    }

    public int launchHumanFighter(ShipType shipType, Location location, Integer unmannedViperId) {
        val humanFighter = (switch (shipType) {
            case VIPER -> galacticaBoard().removeFromReserves(Viper.class);
            case VIPER_MARK_VII -> galacticaBoard().removeFromReserves(ViperMarkVII.class);
            case ASSAULT_RAPTOR -> galacticaBoard().removeFromReserves(AssaultRaptor.class);
            default -> throw new FrakCallTheAdmiralException();
        })
                .map(HumanFighter.class::cast)
                .orElseGet(() -> landUnmannedViper(unmannedViperId));
        galacticaBoard().place(location, humanFighter);
        return humanFighter.id();
    }

    public void placeCivilianShip(Location location) {
        CivilianShip civ = draw(DeckId.CIVILIAN_SHIPS);
        galacticaBoard().place(location, civ);
    }

    public void placeRaider(Location location) {
        game.cylonShips()
                .raider()
                .ifPresent(r -> galacticaBoard().place(location, r));
    }

    @Override
    protected GalacticaBoard board() {
        return galacticaBoard();
    }

    private HumanFighter landUnmannedViper(Integer unmannedViperId) {
        val ship = galacticaBoard().shipInSpace(unmannedViperId, HumanFighter.class);
        galacticaBoard().addToReserves(ship);
        return ship;
    }
}
