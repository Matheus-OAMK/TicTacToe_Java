package com.example.tictactoe.service;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.tictactoe.exception.InvalidGameException;
import com.example.tictactoe.exception.InvalidParamException;
import com.example.tictactoe.exception.NotFoundException;
import com.example.tictactoe.model.Game;
import com.example.tictactoe.model.GamePlay;
import com.example.tictactoe.model.Player;
import com.example.tictactoe.model.TicToe;
import com.example.tictactoe.storage.GameStorage;
import static com.example.tictactoe.model.GameStatus.*;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class GameService implements GameServiceInterface {

  private String generateShortUUID() {
    UUID uuid = UUID.randomUUID();

    byte[] bytes = ByteBuffer.wrap(new byte[16])
        .putLong(uuid.getMostSignificantBits())
        .putLong(uuid.getLeastSignificantBits())
        .array();

    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
}
  
  // Method to create a new game
  @Override
  public Game createGame(Player player) {
    Game game = new Game();
    game.setBoard(new int[3][3]);
    game.setGameId(generateShortUUID());
    game.setPlayer1(player);
    game.setCurrentPlayer(player); //test
    game.setStatus(NEW);
    GameStorage.getInstance().setGame(game);
    return game;
  }

  // Method to connect to a game using gameId
  @Override
  public Game connectToGame(Player player2,String gameId) throws InvalidParamException, InvalidGameException {
    if(!GameStorage.getInstance().getGames().containsKey(gameId)) {
      throw new InvalidParamException("Game with the provided ID does not exist.");
    }
    Game game = GameStorage.getInstance().getGames().get(gameId);

    if(game.getPlayer2() != null) {
      throw new InvalidGameException("Game session is no longer valid");
    }

    game.setPlayer2(player2);
    game.setStatus(IN_PROGRESS);
    GameStorage.getInstance().setGame(game);
    return game;
  }

  // Method to connect to a random game
  @Override
  public Game connectToRandomGame(Player player2) throws NotFoundException {
    Game game = GameStorage.getInstance().getGames().values().stream().filter(it->it.getStatus().equals(NEW)).findFirst().orElseThrow(() -> new NotFoundException("There are no available games right now."));

    game.setPlayer2(player2);
    game.setStatus(IN_PROGRESS);
    GameStorage.getInstance().setGame(game);
    return game;
  }

  // Method to reset a game
  @Override
  public Game resetGame(String gameId) throws NotFoundException {
    if (!GameStorage.getInstance().getGames().containsKey(gameId)) {
        throw new NotFoundException("Game not found");
    }
    Game game = GameStorage.getInstance().getGames().get(gameId);
    game.setBoard(new int[3][3]);  // Reset the board
    game.setWinner(null);   // Reset the winner
    game.setTie(null);         // Reset the tie
    game.setCurrentPlayer(game.getPlayer1()); // Let player1 start the new game
    game.setStatus(IN_PROGRESS);   // Set the status back to IN_PROGRESS
    GameStorage.getInstance().setGame(game);
    return game;
}
  // Method for the gameplay logic
  @Override
  public Game gamePlay(GamePlay gamePlay) throws NotFoundException, InvalidGameException {
    if (!GameStorage.getInstance().getGames().containsKey(gamePlay.getGameId())) {
        throw new NotFoundException("Game not found");
    }

    

    Game game = GameStorage.getInstance().getGames().get(gamePlay.getGameId());

    if (game.getPlayer2() == null) {
      throw new InvalidGameException("please wait for a player to connect before making a move");
  }

    if (game.getStatus().equals(FINISHED)) {
        throw new InvalidGameException("Game is already finished");
    }

    // Determine the player based on the move type
    Player movePlayer = gamePlay.getType().equals(TicToe.X) ? game.getPlayer1() : game.getPlayer2();

    // Check if the player making the move is the currentPlayer
    Player currentPlayer = game.getCurrentPlayer();
    if (currentPlayer == null || !currentPlayer.equals(movePlayer)) {
        throw new InvalidGameException("Please wait your turn");
    }

    int [][] board = game.getBoard();
    board[gamePlay.getCoordinateX()][gamePlay.getCoordinateY()] = gamePlay.getType().getValue();

    Boolean xWinner = checkWinner(game.getBoard(), TicToe.X);
    Boolean oWinner = checkWinner(game.getBoard(), TicToe.O);
    Boolean isTie = checkTie(game.getBoard());
    // if X wins we set the winner and increase the player1 score
    if (xWinner) {
        game.setWinner(TicToe.X);
        game.setStatus(FINISHED);
        game.setPlayer1Wins(game.getPlayer1Wins() + 1);
        // If O wins we set the winner to player2 and icrease the score.
    } else if (oWinner) {
        game.setWinner(TicToe.O);
        game.setStatus(FINISHED);
        game.setPlayer2Wins(game.getPlayer2Wins() + 1);
    } else if (isTie) {
      // If its a tie we increase the score by adding +1 to tie
      game.setTie(isTie);
      game.setStatus(FINISHED);
      game.setTies(game.getTies() + 1);
    }

    // Switch the currentPlayer to the other player after a move is made
    // currentPlayer is used to define whos turn it is
    if (game.getCurrentPlayer().equals(game.getPlayer1())) {
        game.setCurrentPlayer(game.getPlayer2());
    } else {
        game.setCurrentPlayer(game.getPlayer1());
    }

    GameStorage.getInstance().setGame(game);

    return game;
}

  // A method to check winner for tictactoe
  private Boolean checkWinner(int[][] board, TicToe ticToe) {

    int [] boardArray = new int[9];
    int counterIndex = 0;

    for (int i=0; i<board.length; i++) {
      for (int j=0; j<board.length; j++) {
        boardArray[counterIndex] = board[i][j];
        counterIndex++;
      }
    }
    // All possible win combinations
    int[][] winCombinations = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};

    for (int i=0; i < winCombinations.length; i ++) {
      int counter = 0;
      for (int j=0; j < winCombinations[i].length; j++) {
        if (boardArray[winCombinations[i][j]] == ticToe.getValue()) {
          counter++;
          if (counter == 3) {
            return true;
          }
        }
      }
    }
    return false;
  }


  // A method to check if the game ends in a tie.
  private Boolean checkTie(int[][] board) {
  for (int i = 0; i < board.length; i++) {
    for (int j = 0; j < board[i].length; j++) {
      // If there's any cell that is still unmarked (unmarked cells have a value of 0),
      // then it's not a tie.
      if (board[i][j] == 0) {
        return false;
      }
    }
  }
  // If we've gone through the whole board and every cell is marked, then it's a tie.
    return true;
  }

}








