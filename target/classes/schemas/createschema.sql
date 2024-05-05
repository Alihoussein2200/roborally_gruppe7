
SET FOREIGN_KEY_CHECKS = 0;;

CREATE TABLE IF NOT EXISTS Game (
gameID int NOT NULL UNIQUE AUTO_INCREMENT,

name varchar(255),

phase tinyint,
step tinyint,
currentPlayer tinyint NULL,
boardName varchar(50),

PRIMARY KEY (gameID),
FOREIGN KEY (gameID, currentPlayer) REFERENCES Player(gameID, playerID)
);;

CREATE TABLE IF NOT EXISTS Player (
gameID int NOT NULL,
playerID tinyint NOT NULL,

name varchar(255),
colour varchar(31),

positionX int,
positionY int,
heading tinyint,
checkpoint tinyint,

PRIMARY KEY (gameID, playerID),
FOREIGN KEY (gameID) REFERENCES Game(gameID)
);;

CREATE TABLE IF NOT EXISTS CardInPlayersHand (
GameID INT NOT NULL,
PlayerID TINYINT NOT NULL,
CardNo INT,
CardValue INT,

PRIMARY KEY(PlayerID, GameID, CardNo),
FOREIGN KEY(gameID, playerID) REFERENCES Player(gameID, playerID)
);;

CREATE TABLE IF NOT EXISTS CardInPlayersRegister (
GameID INT NOT NULL,
PlayerID TINYINT NOT NULL,
RegisterNo INT,
CardValue INT,

PRIMARY KEY(PlayerID, GameID,RegisterNo),
FOREIGN KEY(gameID, playerID) REFERENCES Player(gameID, playerID)
);;



SET FOREIGN_KEY_CHECKS = 1;;

