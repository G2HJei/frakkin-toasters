package xyz.zlatanov.frakkintoasters;

import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.board.PegasusBoard;
import xyz.zlatanov.frakkintoasters.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.ship.*;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.Location.*;

@Getter
@Accessors(fluent = true)
public class Game {
    private ObjectiveCard objective;
    private GalacticaBoard galacticaBoard = new GalacticaBoard();
    private PegasusBoard pegasusBoard = new PegasusBoard();
    private Deck<CivilianShip> civilianShipsDeck = new Deck<>();

    public void objective(ObjectiveCard objective) {
        if (this.objective != null) {
            throw new FrakCallTheAdmiralException();
        }
        this.objective = objective;
    }

    public void setupCivilianShipsDeck() {
        civilianShipsDeck.add(List.of(
                new CivilianShip(0, 0, 0),
                new CivilianShip(0, 0, 0),
                new CivilianShip(0, 0, 1),
                new CivilianShip(0, 0, 1),
                new CivilianShip(0, 0, 1),
                new CivilianShip(0, 0, 1),
                new CivilianShip(0, 0, 1),
                new CivilianShip(0, 0, 1),
                new CivilianShip(0, 0, 2),
                new CivilianShip(0, 0, 2),
                new CivilianShip(0, 1, 1),
                new CivilianShip(1, 0, 1)));
    }

    public void setupGalacticaBoard() {
        galacticaBoard.addToReserves(List.of(new Viper(), new Viper(), new Viper(), new Viper(), new Raptor(), new Raptor(), new Raptor(), new Raptor(), new AssaultRaptor()));
        galacticaBoard.addToDamagedShips(List.of(new ViperMarkVII(), new ViperMarkVII(), new ViperMarkVII(), new ViperMarkVII()));
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, new Viper());
        galacticaBoard.place(GALACTICA_SPACE_6_OCLOCK, new Viper());
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, List.of(civilianShipsDeck.draw(), civilianShipsDeck.draw()));
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, new Basestar());
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, List.of(new Raider(), new Raider(), new Raider()));
    }

}
