package com.example.tictactoe.model;

import lombok.Data;

@Data
public class Game {

  private String gameId;
  private Player player2;
  private Player player1;
  private int player1Wins;
  private int player2Wins;
  private int ties;
  private GameStatus status;
  private int [][] board;
  private TicToe winner;
  private Boolean tie;
  private Player currentPlayer; 
}
