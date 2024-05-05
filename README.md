# PISD  02362 - Roborally - Gruppe7



## Features for our current version of RoboRally:
Robots can push each other
Walls - robots cant go through a wall or be pushed through a wall
Checkpoints - the players have to pick up the checkpoints i the correct order to win
Conveyor belts - robots will get moved if they're on a conveyor belt
Save game - You can save your game from - File -> Save game
Load game; You can load a previous game at the start of the program

## Boards
Risky crossing
Sprint cramp


## How to connect to a database, in order to save and load games
Create a SQL database on your local machine

Open the project in IntelliJ
go to -> roborally/src/main/java/dk.dtu.compute.se.pisd/roborally/dal/Connector

Change the following variables to connect your database:

private static final String HOST     = "YOUR_HOST_NAME";
private static final int    PORT     = YOUR_PORT_NUMBER;
private static final String DATABASE = "YOUR_DATABASE_NAME
private static final String USERNAME = "YOUR_USERNAME";
private static final String PASSWORD = "YOUR_PASSWORD";


### HOW TO RUN THE GAME
Open the project in IntelliJ

Go to -> roborally/src/main/java/dk.dtu.compute.se.pisd/roborally/startRoborally

press shift+F10 (works on windows)
Or press the green play button in the top right corner
