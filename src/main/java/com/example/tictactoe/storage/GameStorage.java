package com.example.tictactoe.storage;

import java.util.HashMap;
import java.util.Map;

import com.example.tictactoe.model.Game;

public class GameStorage {
  
  private static Map<String, Game> games;
  private static GameStorage instance;

  private GameStorage(){
    games = new HashMap<>();
  }

  public static synchronized GameStorage getInstance(){
    if (instance == null) {
      instance = new GameStorage();
    }
    return instance;
  }

  public Map<String, Game> getGames() {
    return games;
  }
  public void setGame(Game game) {
    games.put(game.getGameId(), game);
  }
}
