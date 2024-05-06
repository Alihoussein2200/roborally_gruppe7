package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;

public class Pit extends FieldAction {

    /**
     * Executes the pit action for a given space. If a player is present,
     * their cards are cleared and their position is reset to a defined start or reboot space.
     *
     * @param gameController the controller managing game logic and actions
     * @param space the game board space associated with this action
     * @return true if a player was present and the action executed, false otherwise
     *
     * NOT IMPLEMENTED FULLY
     */
    @Override
    public boolean doAction(GameController gameController, Space space) {
        Player currentPlayer = space.getPlayer();
        if (currentPlayer != null) {
            gameController.clearPlayersCards(currentPlayer);
            currentPlayer.setSpace(gameController.rebootOrStart(space, currentPlayer));
            return true;
        }
        return false;
    }
}
