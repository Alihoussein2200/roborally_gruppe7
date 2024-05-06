package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Space;

/**
 * Represents a starting space action on the game board.
 * This class provides functionality associated with the starting spaces where players begin the game or respawn.
 * Typically used to handle player placement at the start of the game or after events like rebooting.
 */
public class StartSpace extends FieldAction {
    @Override
    public boolean doAction(GameController gameController, Space space) {
        return false;
    }
}
