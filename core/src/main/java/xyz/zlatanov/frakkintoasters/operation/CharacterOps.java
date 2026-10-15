package xyz.zlatanov.frakkintoasters.operation;

import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;

public class CharacterOps extends GameOps {

    private final Character character;

    CharacterOps(Game game, Character character) {
        super(game);
        this.character = character;
    }

    public void moveTo(Location location) {
        assert !location.isSpaceLocation();
        game.boards().all().forEach(b -> b.remove(character));
        game.boards().all().stream()
                .filter(b -> b.locations().contains(location))
                .findFirst()
                .orElseThrow(FrakCallTheAdmiralException::new)
                .place(location, character);
    }

    public void pilot(int humanFighterId) {
        galacticaBoard().remove(character);
        galacticaBoard()
                .shipInSpace(humanFighterId, HumanFighter.class)
                .pilot(character);
    }
}
