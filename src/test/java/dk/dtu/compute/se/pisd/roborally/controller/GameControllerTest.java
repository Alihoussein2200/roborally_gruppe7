package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameControllerTest {
    Board board = new Board(10, 10, 2, "board");
    GameController gamecontroller = new GameController(board);


    @Test
    void movePlayerTurnAndMove() {
        Player player1 = new Player(board, null,"Player1");
        player1.setSpace(gamecontroller.board.getSpace(0,0));
        assertEquals(player1.getSpace(), gamecontroller.board.getSpace(0, 0));
        gamecontroller.movePlayer(player1, 3, false);
        assertEquals(player1.getSpace(), gamecontroller.board.getSpace(0,3));
        player1.setHeading(Heading.EAST);
        gamecontroller.movePlayer(player1, 2, false);
        assertEquals(player1.getSpace(), gamecontroller.board.getSpace(2,3));
        
        Assertions.assertNull(board.getSpace(0,0).getPlayer());
        Assertions.assertNull(board.getSpace(0,3).getPlayer());

    }

    @Test
    void movePlayerBackward(){
        Player player1 = new Player(board, null,"Player1");
        player1.setSpace(gamecontroller.board.getSpace(6,6));
        gamecontroller.movePlayer(player1, 2, true);
        assertEquals(player1.getSpace(), gamecontroller.board.getSpace(6,4),"Player1 should be at space (6,4)");
    }

    @Test
    void pushPlayer() {
        Player player1 = new Player(board, null,"Player1");
        player1.setSpace(gamecontroller.board.getSpace(0,0));
        assertEquals(player1.getSpace(), gamecontroller.board.getSpace(0,0));
        gamecontroller.pushRobot(player1, player1.getHeading());
        assertEquals(player1.getSpace(), gamecontroller.board.getSpace(0,1), "Player1 should be at space (0,1)");

    }

    @Test
    void turnRight() {
        Player player1 = new Player(board, null,"Player1");
        assertEquals(player1.getHeading(), Heading.SOUTH);
        gamecontroller.turnRight(player1);
        assertEquals(player1.getHeading(), Heading.WEST, "Player1 should be facing West");
    }

    @Test
    void turnLeft() {
        Player player1 = new Player(board, null,"Player1");
        assertEquals(player1.getHeading(), Heading.SOUTH);
        gamecontroller.turnLeft(player1);
        assertEquals(player1.getHeading(), Heading.EAST, "Player1 should be facing East");
    }

    @Test
    void makeUTurn() {
        Player player1 = new Player(board, null, "Player1");
        player1.setHeading(Heading.NORTH);
        gamecontroller.makeUTurn(player1);
        assertEquals(player1.getHeading(), Heading.SOUTH, "Player1 should be facing South");
    }

}