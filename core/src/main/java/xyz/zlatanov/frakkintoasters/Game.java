package xyz.zlatanov.frakkintoasters;

import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.board.CylonFleetBoard;
import xyz.zlatanov.frakkintoasters.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.board.PegasusBoard;
import xyz.zlatanov.frakkintoasters.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.damage.PegasusDamage;
import xyz.zlatanov.frakkintoasters.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.ship.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static xyz.zlatanov.frakkintoasters.DestinationCard.*;
import static xyz.zlatanov.frakkintoasters.Location.*;

@Getter
@Accessors(fluent = true)
public class Game {
    private ObjectiveCard         objective;
    private GalacticaBoard        galacticaBoard          = new GalacticaBoard();
    private PegasusBoard          pegasusBoard            = new PegasusBoard();
    private CylonFleetBoard       cylonFleetBoard         = new CylonFleetBoard();
    private Deck<CivilianShip>    civilianShipsDeck       = new Deck<>();
    private Deck<BasestarDamage>  basestarDamageTokenDeck = new Deck<>();
    private Deck<PegasusDamage>   pegasusDamageDeck       = new Deck<>();
    private Deck<GalacticaDamage> galacticaDamageDeck     = new Deck<>();
    private Deck<DestinationCard> destinationDeck         = new Deck<>();
    private int                   nukes                   = 3;
    private int                   basestars               = 2;
    private int                   centurions              = 4;


    public void objective(ObjectiveCard objective) {
        if (this.objective != null) {
            throw new FrakCallTheAdmiralException();
        }
        this.objective = objective;
    }

    public void setupCivilianShipsDeck() {
        IntStream.range(0, 2).forEach(i -> civilianShipsDeck.add(new CivilianShip(0, 0, 0)));
        IntStream.range(0, 2).forEach(i -> civilianShipsDeck.add(new CivilianShip(0, 0, 2)));
        IntStream.range(0, 6).forEach(i -> civilianShipsDeck.add(new CivilianShip(0, 0, 1)));
        civilianShipsDeck.add(new CivilianShip(0, 1, 1));
        civilianShipsDeck.add(new CivilianShip(1, 0, 1));
        civilianShipsDeck.shuffle();
    }

    public void setupGalacticaBoard() {
        galacticaBoard.addToReserves(List.of(new Viper(), new Viper(), new Viper(), new Viper(), new Raptor(), new Raptor(), new Raptor(), new Raptor(), new AssaultRaptor()));
        galacticaBoard.addToDamagedShips(List.of(new ViperMarkVII(), new ViperMarkVII(), new ViperMarkVII(), new ViperMarkVII()));
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, new Viper());
        galacticaBoard.place(GALACTICA_SPACE_6_OCLOCK, new Viper());
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, List.of(civilianShipsDeck.draw(), civilianShipsDeck.draw()));
        basestars--;
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, new Basestar());
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, List.of(new Raider(), new Raider(), new Raider()));
    }

    public void setupExtraTokens() {
        basestarDamageTokenDeck.add(Arrays.stream(BasestarDamage.values()).toList());
        basestarDamageTokenDeck.shuffle();
        pegasusDamageDeck.add(Arrays.stream(PegasusDamage.values()).toList());
        pegasusDamageDeck.shuffle();
        galacticaDamageDeck.add(Arrays.stream(GalacticaDamage.values()).toList());
        galacticaDamageDeck.shuffle();
        destinationDeck.add(Arrays.stream(DestinationCard.values()).toList());
        //add repeated cards. todo: DOUBLE CHECK!!!
        destinationDeck.add(List.of(REMOTE_PLANET, ICY_MOON, BARREN_PLANET, TYLIUM_PLANET, TYLIUM_PLANET, TYLIUM_PLANET));
        destinationDeck.shuffle();
    }

}
