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
package dk.dtu.compute.se.pisd.roborally.view;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.controller.*;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;


/**
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class SpaceView extends StackPane implements ViewObserver {

    final public static int SPACE_HEIGHT = 40; // 60; // 75;
    final public static int SPACE_WIDTH = 40; // 60; // 75;

    public final Space space;

    /**
     * Constructor for SpaceView. Creates a board in a checkerboard pattern.
     * @Author Ekkart Kindler
     * @param space The actual Space that needs GUI.
     */
    public SpaceView(@NotNull Space space) {
        this.space = space;

        // XXX the following styling should better be done with styles
        this.setPrefWidth(SPACE_WIDTH);
        this.setMinWidth(SPACE_WIDTH);
        this.setMaxWidth(SPACE_WIDTH);

        this.setPrefHeight(SPACE_HEIGHT);
        this.setMinHeight(SPACE_HEIGHT);
        this.setMaxHeight(SPACE_HEIGHT);

        if ((space.x + space.y) % 2 == 0) {
           this.setStyle("-fx-background-color: grey;");
        } else {
            this.setStyle("-fx-background-color: darkgrey");
        }

        // updatePlayer();
        // This space view should listen to changes of the space
        space.attach(this);
        update(space);
    }

    /**
     * Draws the player on the board.
     * The player's image is determined by their color.
     * The image is heading north by default.
     * If the player is facing WEST, their image is rotated 270 degrees.
     * If the player is facing EAST, their image is rotated 90 degrees.
     * If the player is facing SOUTH, their image is rotated 180 degrees.
     * The width and height of the player's image are adjusted to fit the space on the board.
     *
     * @Author Emil s230995
     * @throws FileNotFoundException
     */

    private void drawPlayer() {
        Player player = space.getPlayer();
        if (player != null) {
            ImageView imageView = new ImageView();
            try {
                String color = player.getColor();
                String imagePath = String.format("/src/main/resources/view.png/Robot%s.png", color.substring(0, 1).toUpperCase() + color.substring(1).toLowerCase());
                imageView.setImage(new Image(new FileInputStream(System.getProperty("user.dir") + imagePath)));
            } catch (FileNotFoundException e) {
                // Handle exception
            }

            switch (player.getHeading()) {
                case WEST:
                    imageView.setRotate(270);
                    break;
                case EAST:
                    imageView.setRotate(90);
                    break;
                case SOUTH:
                    imageView.setRotate(180);
                    break;
            }

            imageView.fitWidthProperty().setValue(SPACE_WIDTH);
            imageView.fitHeightProperty().setValue(SPACE_HEIGHT);

            this.getChildren().add(imageView);
        }
    }



    /**
     *
     * Draws walls on the board.
     * The image for walls is heading north by default.
     * Each wall is represented by an image rotated according to its direction.
     * The width and height of each image are adjusted to fit the space on the board.
     *
     * @Author Emil s230995, Abaas
     * @throws FileNotFoundException if the image file for the wall is not found.
     */

    private void drawWalls() {
        try {
            Image image = new Image(new FileInputStream(System.getProperty("user.dir") + "/src/main/resources/view.png/wallHeadingNorth.png"));

            for (Heading wall : space.getWalls()) {
                ImageView imageView = new ImageView(image);
                imageView.setRotate(getRotationAngle(wall));
                imageView.fitHeightProperty().setValue(SPACE_HEIGHT);
                imageView.fitWidthProperty().setValue(SPACE_WIDTH);
                this.getChildren().add(imageView);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the rotation angle for the wall image.
     * @Author Emil s230995
     * @param wall The direction of the wall.
     * @return The rotation angle for the wall image.
     */

    private double getRotationAngle(Heading wall) {
        switch (wall) {
            case EAST:
                return 90;
            case SOUTH:
                return 180;
            case WEST:
                return 270;
            default:
                return 0;
        }
    }



    @Override
    public void updateView(Subject subject) {
        if (subject == this.space) {
            this.getChildren().clear();
          //  drawBackground();
            if (space.getActions().size() > 0) {
                drawBoardElements();
            }
            drawWalls();
           drawPlayer();
        }
    }

    /**
     * Draws the board elements on the board.
     * The board elements are drawn according to the type of action on the space.
     * @Author Emil, Abaas, Jasminder, Ali
     */
    public void drawBoardElements() {
        ImageView imageView = new ImageView();

        switch (space.getActions().get(0).getClass().getName()) {
            case "dk.dtu.compute.se.pisd.roborally.controller.ConveyorBelt":
                ConveyorBelt conveyorBelt = (ConveyorBelt) space.getActions().get(0);
                drawConveyorBelt(conveyorBelt, imageView);
                break;
            case "dk.dtu.compute.se.pisd.roborally.controller.Checkpoint":
                Checkpoint checkpoint = (Checkpoint) space.getActions().get(0);
                drawCheckpoint(checkpoint, imageView);
                break;
            case "dk.dtu.compute.se.pisd.roborally.controller.StartSpace":
                drawStartSpace(imageView);
                break;
            case "dk.dtu.compute.se.pisd.roborally.controller.Pit":
                drawPit(imageView);
                break;
            case "dk.dtu.compute.se.pisd.roborally.controller.Reboot":
                Reboot reboot = (Reboot) space.getActions().get(0);
                drawReboot(reboot, imageView);
                break;
            default:
                break;
        }
        if (imageView.getImage() != null) {
            FitPictureToSpace(imageView);
        }

    }

    /**
     * Draws the conveyor belt on the board.
     * @param conveyorBelt
     * @param imageView
     *
     * @Author Emil, Abaas,
     */
    private void drawConveyorBelt(ConveyorBelt conveyorBelt, ImageView imageView) {
        try {
            String imagePath = conveyorBelt.getDoublecb() ?
                    "/src/main/resources/view.png/NorthConveyorbeltDouble.png" :
                    "/src/main/resources/view.png/NorthConveyorbelt.png";
            imageView.setImage(new Image(new FileInputStream(System.getProperty("user.dir") + imagePath)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        switch (conveyorBelt.getHeading()) {
            case EAST:
                imageView.setRotate(90);
                break;
            case SOUTH:
                imageView.setRotate(180);
                break;
            case WEST:
                imageView.setRotate(270);
                break;
        }
    }


    /**
     * Draws the checkpoint on the board.
     * @param checkpoint
     * @param imageView
     *
     * @author Jasminder, Ali
     */
    private void drawCheckpoint(Checkpoint checkpoint, ImageView imageView) {
        try {
            String imagePath = "/src/main/resources/view.png/checkpoint" + checkpoint.getCheckpointNr() + ".png";
            imageView.setImage(new Image(new FileInputStream(System.getProperty("user.dir") + imagePath)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Draws the start space on the board.
     * @param imageView
     *
     * @author Emil
     */

    private void drawStartSpace(ImageView imageView) {
        try {
            imageView.setImage(new Image(new FileInputStream(System.getProperty("user.dir") + "/src/main/resources/view.png/startGear.PNG")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Draws the pit on the board.
     * @param imageView
     *
     * @author Abaas
     */
    private void drawPit(ImageView imageView) {
        try {
            imageView.setImage(new Image(new FileInputStream(System.getProperty("user.dir") + "/src/main/resources/view.png/pit.png")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * Draws the reboot on the board.
     * @param reboot
     * @param imageView
     *
     * @author Jasminder
     */
    private void drawReboot(Reboot reboot, ImageView imageView) {
        try {
            imageView.setImage(new Image(new FileInputStream(System.getProperty("user.dir") + "/src/main/resources/view.png/rebootNorth.png")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        switch (reboot.getHeading()) {
            case EAST:
                imageView.setRotate(90);
                break;
            case SOUTH:
                imageView.setRotate(180);
                break;
            case WEST:
                imageView.setRotate(270);
                break;
        }
    }

    /**
     * Fits the picture to the space on the board.
     * @Author Emil
     * @param imageView
     */
    private void FitPictureToSpace(ImageView imageView) {
        imageView.fitHeightProperty().setValue(SPACE_HEIGHT);
        imageView.fitWidthProperty().setValue(SPACE_WIDTH);
        this.getChildren().add(imageView);
    }





}

