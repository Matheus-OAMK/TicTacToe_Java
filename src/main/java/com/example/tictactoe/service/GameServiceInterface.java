package com.example.tictactoe.service;

import com.example.tictactoe.model.Game;
import com.example.tictactoe.model.GamePlay;
import com.example.tictactoe.model.Player;
import com.example.tictactoe.exception.InvalidParamException;
import com.example.tictactoe.exception.InvalidGameException;
import com.example.tictactoe.exception.NotFoundException;

public interface GameServiceInterface {
    Game createGame(Player player); // To start an instance of the game
    Game connectToGame(Player player2, String gameId) throws InvalidParamException, InvalidGameException; // to connect to a game via gameID
    Game gamePlay(GamePlay gamePlay) throws NotFoundException, InvalidGameException; //to make moves
    Game resetGame(String gameId) throws NotFoundException;  // to be able to reset the game if the game is over
    Game connectToRandomGame(Player player) throws NotFoundException;  // A method to connect without an Id
}
