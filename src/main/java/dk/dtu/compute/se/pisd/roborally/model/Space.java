package dk.dtu.compute.se.pisd.roborally.model;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;

import java.util.ArrayList;
import java.util.List;

public class Space extends Subject {

    public final Board board;
    public final int x;
    public final int y;
    private Player player;
    private List<Heading> walls = new ArrayList<>();
    private List<FieldAction> actions = new ArrayList<>();

    /**
     * The constructor for the space
     * @param board
     * @param x
     * @param y
     */
    public Space(Board board, int x, int y) {
        this.board = board;
        this.x = x;
        this.y = y;
        player = null;
        actions = getActions();
        walls = getWalls();
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * Sets the player on the space
     * @param player
     */
    public void setPlayer(Player player) {
        Player oldPlayer = this.player;
        if (player != oldPlayer &&
                (player == null || board == player.board)) {
            this.player = player;
            if (oldPlayer != null) {
                oldPlayer.setSpace(null);
            }
            if (player != null) {
                player.setSpace(this);
            }
            notifyChange();
        }
    }


    public List<Heading> getWalls() {
        return walls;
    }



    public List<FieldAction> getActions() {
        return actions;
    }

    void playerChanged() {
        notifyChange();
    }
}
