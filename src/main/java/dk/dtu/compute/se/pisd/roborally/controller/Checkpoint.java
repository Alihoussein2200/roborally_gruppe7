package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Space;

public class Checkpoint extends FieldAction {
    private int checkpointNr;
    public int getCheckpointNr(){return checkpointNr;}



    /**
     * Performs the checkpoint action on the specified space in the game.
     *
     * @param gameController the game controller managing the game
     * @param space          the space where the checkpoint action is performed
     * @return true if the action was successfully executed, false otherwise
     *
     * @author Ali, Jasminder
     */
    @Override
    public boolean doAction(GameController gameController, Space space) {
        if(space.getPlayer() != null){
            int playersCPTokens = space.getPlayer().getCheckpointTokens();
            if(playersCPTokens == getCheckpointNr()-1){
                space.getPlayer().setCheckpointTokens(getCheckpointNr());
                return true;
            }
        }
        return false;
    }
}