package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.board.Resource;

import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FUEL;

// "The Admiral may risk ships to roll a die" (rule 13.5): with the ships in the "Reserves", a roll of at least
// {@code target} gains the reward, otherwise the risked ships are lost.
public record RiskShipsEvent(int playerNumber, Risk risk) implements PlayerEvent {

    public enum Risk {
        // "Cylon Refinery": "The Admiral may risk 2 vipers to roll a die. If 6 or higher, gain 2 fuel. Otherwise, damage 2 vipers."
        CYLON_REFINERY(true, 2, 6, FUEL, 2),
        // "Icy Moon": "The Admiral may risk 1 raptor to roll a die. If 3 or higher, gain 1 food. Otherwise, destroy 1 raptor."
        ICY_MOON(false, 1, 3, FOOD, 1),
        // "Tylium Planet": "The Admiral may risk 1 raptor to roll a die. If 3 or higher, gain 2 fuel. Otherwise, destroy 1 raptor."
        TYLIUM_PLANET(false, 1, 3, FUEL, 2);

        private final boolean vipers;
        private final int     count;
        private final int     target;
        private final Resource reward;
        private final int     amount;

        Risk(boolean vipers, int count, int target, Resource reward, int amount) {
            this.vipers = vipers;
            this.count = count;
            this.target = target;
            this.reward = reward;
            this.amount = amount;
        }

        public boolean vipers() {
            return vipers;
        }

        public int count() {
            return count;
        }

        public int target() {
            return target;
        }

        public Resource reward() {
            return reward;
        }

        public int amount() {
            return amount;
        }
    }
}
