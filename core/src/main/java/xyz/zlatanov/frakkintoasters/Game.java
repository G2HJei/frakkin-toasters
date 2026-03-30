package xyz.zlatanov.frakkintoasters;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.state.board.BoardsHolder;
import xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.*;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

@Getter
@Accessors(fluent = true)
public class Game {
    // todo inject boards, decks and counters for testing purposes?
    // todo separate decks and counters in own classes?
    // todo add players, current player, turns
    private ObjectiveCard objective;
    private BoardsHolder  boards;
    private DecksHolder   decks;
    private ShipsHolder   ships;
    private int           nukes;
    @Setter
    private Character     president;
    @Setter
    private Character     admiral;
    @Setter
    private Character     cag;


    public void objective(ObjectiveCard objective) {
        if (this.objective != null) {
            throw new FrakCallTheAdmiralException();
        }
        this.objective = objective;
    }

    public void setupGalacticaBoard() {
        boards.galactica()
                //todo improve raptor - assault raptor handling
                .addToReserves(List.of(new Viper(), new Viper(), new Viper(), new Viper(), new Raptor(), new Raptor(), new Raptor(), new Raptor(), new AssaultRaptor()))
                .addToDamagedShips(List.of(new ViperMarkVII(), new ViperMarkVII(), new ViperMarkVII(), new ViperMarkVII()))
                .place(GALACTICA_SPACE_4_OCLOCK, new Viper())
                .place(GALACTICA_SPACE_6_OCLOCK, new Viper())
                .place(GALACTICA_SPACE_2_OCLOCK, List.of(decks.civilianShips().draw(), decks.civilianShips().draw()))
                .place(GALACTICA_SPACE_8_OCLOCK, ships.basestar())
                .place(GALACTICA_SPACE_8_OCLOCK, List.of(ships.raider(), ships.raider(), ships.raider(), ships.raider()));
    }


}
