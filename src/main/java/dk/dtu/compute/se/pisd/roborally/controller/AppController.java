/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.designpatterns.observer.Observer;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;

import dk.dtu.compute.se.pisd.roborally.RoboRally;

import dk.dtu.compute.se.pisd.roborally.dal.GameInDB;
import dk.dtu.compute.se.pisd.roborally.dal.RepositoryAccess;
import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard;
import dk.dtu.compute.se.pisd.roborally.model.*;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import java.io.File;

/**
 * The controller for the application itself. Responsible for overall actions of
 * the application such as starting a new game or shutting down the application.
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class AppController implements Observer {

    final private List<Integer> PLAYER_NUMBER_OPTIONS = Arrays.asList(2, 3, 4, 5, 6);
    final private List<String> PLAYER_COLORS = Arrays.asList("red", "green", "blue", "orange", "grey", "magenta");

    final private RoboRally roboRally;

    private GameController gameController;

    /**
     * The constructor for AppController.
     *
     * @param roboRally the GUI.
     */
    public AppController(@NotNull RoboRally roboRally) {
        this.roboRally = roboRally;
    }

    /**
     * Method to start a new game. Will present a dropdown menu with number of
     * players and create the chosen amount of players along with other elements
     * required for the game to start such as a board and the GUI.
     *
     */
    public void newGame() {
        File boardFolder = new File("src/main/resources/boards");
        String[] fileNames = boardFolder.list();
        String[] boardNames = new String[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            boardNames[i] = fileNames[i].replace(".json", "");
        }
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(PLAYER_NUMBER_OPTIONS.get(0), PLAYER_NUMBER_OPTIONS);
        dialog.setTitle("Player number");
        dialog.setHeaderText("Select number of players");
        Optional<Integer> result = dialog.showAndWait();

        if (result.isPresent()) {
            if (gameController != null) {
                // The UI should not allow this, but in case this happens anyway.
                // give the user the option to save the game or abort this operation!
                if (!stopGame()) {
                    return;
                }
            }

            ChoiceDialog<String> boardDialog = new ChoiceDialog<>(boardNames[0], boardNames);
            boardDialog.setTitle("Board");
            boardDialog.setHeaderText("Select board");
            Optional<String> boardResult = boardDialog.showAndWait();

            // XXX the board should eventually be created programmatically or loaded from a
            // file
            // here we just create an empty board with the required number of players.
            if (boardResult.isPresent()) {
                ArrayList<Space> startfields = new ArrayList<>();


                try {
                    Command[] commands = Command.values();
                    Board board = LoadBoard.loadBoard(boardResult.get());
                    gameController = new GameController(board);
                    //noinspection DuplicatedCode
                    for(int i = 0; i<board.width;i++){
                        for(int j = 0; j<board.height;j++){
                            for(FieldAction action : board.getSpace(i,j).getActions()){
                                if(action instanceof StartSpace){
                                    startfields.add(board.getSpace(i,j));
                                }else if(action instanceof Checkpoint){
                                    board.setCheckpoints(board.getCheckpoints()+1);

                                }else if(action instanceof RebootTokens){
                                    board.getRebootTokens().add(board.getSpace(i,j));
                                }
                            }
                        }

                    }
                    int no = result.get();
                    for (int i = 0; i < no; i++) {
                        Player player = new Player(board, PLAYER_COLORS.get(i), "Player " + (i + 1));
                        board.addPlayer(player);
                        player.setSpace(startfields.get(i%startfields.size()));

                    }


                    // XXX: V2
                    // board.setCurrentPlayer(board.getPlayer(0));
                    gameController.startProgrammingPhase();

                    roboRally.createBoardView(gameController);
                } catch (Exception e) {
                }

            }
        }
    }


    public void saveGame() {

        Boolean isSame = false;

        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Save game");
        dialog.setHeaderText("Save file");
        dialog.setContentText("Save game as:");


        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            gameController.board.setName(result.get());
            List<GameInDB> gameIDs = RepositoryAccess.getRepository().getGames();

            for (GameInDB gameID : gameIDs) {
                if (gameController.board.getGameId() != null) {
                    if (gameID.id == gameController.board.getGameId()) {
                        isSame = true;
                    }
                }
            }
            if (isSame) {
                RepositoryAccess.getRepository().updateGameInDB(gameController.board);
            } else {
                RepositoryAccess.getRepository().createGameInDB(gameController.board);
            }
        }


    }


    public void loadGame() {

        if (gameController == null) {
            GameInDB currentGame = null;
            List<GameInDB> gameIDs = RepositoryAccess.getRepository().getGames();
            List<String> gameName = new ArrayList<String>();
            for (GameInDB game : gameIDs) {
                gameName.add(game.name);
            }
            ChoiceDialog<String> dialog = new ChoiceDialog<>(gameName.get(0), gameName);

            dialog.setTitle("Load game");
            dialog.setHeaderText("Choose a game");
            Optional<String> result = dialog.showAndWait();
            try {
                for (GameInDB game : gameIDs) {
                    if (game.name == result.get()) {
                        currentGame = game;
                    }
                }
                Board board = RepositoryAccess.getRepository().loadGameFromDB(currentGame.id);

                gameController = new GameController(RepositoryAccess.getRepository().loadGameFromDB(currentGame.id));

                roboRally.createBoardView(gameController);

            } catch (Exception e) {
            }
            try {
                ArrayList<Space> startfields = new ArrayList<>();

                Board board = gameController.board;
                //noinspection DuplicatedCode
                for (int i = 0; i < board.width; i++) {
                    for (int j = 0; j < board.height; j++) {
                        for (FieldAction action : board.getSpace(i, j).getActions()) {
                            if (action instanceof StartSpace) {
                                startfields.add(board.getSpace(i, j));
                            } else if (action instanceof Checkpoint) {
                                board.setCheckpoints(board.getCheckpoints() + 1);

                            } else if (action instanceof RebootTokens) {
                                board.getRebootTokens().add(board.getSpace(i, j));
                            }
                        }
                    }

                }

            } catch (Exception e) {

            }
            gameController.startProgrammingPhase();
        }
    }

    /**
     * Stop playing the current game, giving the user the option to save the game or
     * to cancel stopping the game. The method returns true if the game was
     * successfully stopped (with or without saving the game); returns false, if the
     * current game was not stopped. In case there is no current game, false is
     * returned.
     *
     * @return true if the current game was stopped, false otherwise
     */
    public boolean stopGame() {
        if (gameController != null) {

            // here we save the game (without asking the user).
            saveGame();

            gameController = null;
            roboRally.createBoardView(null);
            return true;
        }
        return false;
    }

    /**
     * Method for the purpose of exiting the game/application. This will create an
     * alert as a GUI element asking if the user wants to exit the game.
     */
    public void exit() {
        if (gameController != null) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Exit RoboRally?");
            alert.setContentText("Are you sure you want to exit RoboRally?");
            Optional<ButtonType> result = alert.showAndWait();

            if (!result.isPresent() || result.get() != ButtonType.OK) {
                return; // return without exiting the application
            }
        }

        // If the user did not cancel, the RoboRally application will exit
        // after the option to save the game
        if (gameController == null || stopGame()) {
            Platform.exit();
        }
    }

    /**
     * Get method for the gameController object to see if it's null.
     *
     * @return the GameController object
     */
    public boolean isGameRunning() {
        return gameController != null;
    }

    @Override
    public void update(Subject subject) {
        // XXX do nothing for now
    }

    public boolean GameIsDone(Player player) {
        if (gameController != null) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Game is done");
            alert.setContentText(player.getName() + " has won the game.");
            Optional<ButtonType> result = alert.showAndWait();

            gameController = null;
            roboRally.createBoardView(null);
            return true;
        }
        return false;
    }


}
