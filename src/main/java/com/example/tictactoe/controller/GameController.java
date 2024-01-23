package com.example.tictactoe.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.tictactoe.service.GameService;
import com.example.tictactoe.storage.GameStorage;
import com.example.tictactoe.controller.dto.ConnectRequest;
import com.example.tictactoe.exception.InvalidGameException;
import com.example.tictactoe.exception.InvalidParamException;
import com.example.tictactoe.exception.NotFoundException;
import com.example.tictactoe.model.Game;
import com.example.tictactoe.model.GamePlay;
import com.example.tictactoe.model.Player;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/game")
public class GameController {
  
  private final GameService gameService;
  private final SimpMessagingTemplate simpMessagingTemplate;

  @ExceptionHandler(InvalidGameException.class)
  public ResponseEntity<Map<String, String>> handleInvalidGameException(InvalidGameException ex) {
    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("error", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
}

  @ExceptionHandler(InvalidParamException.class)
  public ResponseEntity<Map<String, String>> handleInvalidParamException(InvalidParamException ex) {
    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("error", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException ex) {
    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("error", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @PostMapping("/start")
  public ResponseEntity<Game> start( @RequestBody Player player ) {
    log.info("start game request: {}", player);
    return ResponseEntity.ok(gameService.createGame(player));
  }

  @PostMapping("/connect")
  public ResponseEntity<Game> connect( @RequestBody ConnectRequest request ) throws InvalidParamException , InvalidGameException {
    log.info("connect request: {}", request);
    Game game = gameService.connectToGame(request.getPlayer(), request.getGameId());
    
    // Notify Player1 that Player2 has joined
    simpMessagingTemplate.convertAndSend("/topic/game-join/" + request.getGameId(), request.getPlayer().getLogin() + " has joined the game!" );

    // send update of the state of the game for both players
    simpMessagingTemplate.convertAndSend("/topic/game-progress/" + request.getGameId(), game);
    return ResponseEntity.ok(game);
  }

  @PostMapping("/connect/random")
  public ResponseEntity<Game> connectRandom(@RequestBody Player player) throws NotFoundException {
    log.info("connect random: {}", player);
    Game game = gameService.connectToRandomGame(player);
    // Notify Player1 that Player2 has joined
    simpMessagingTemplate.convertAndSend("/topic/game-join/" + game.getGameId(), player.getLogin() + " has joined the game!" );

    // send update of the state of the game for both players
    simpMessagingTemplate.convertAndSend("/topic/game-progress/" + game.getGameId(), game);
    
    return ResponseEntity.ok( game);
  }

  @PostMapping("/gameplay")
  public ResponseEntity<Game> gamePlay(@RequestBody GamePlay request) throws NotFoundException, InvalidGameException {
    log.info("gameplay: {}", request);
    Game game = gameService.gamePlay(request);
    simpMessagingTemplate.convertAndSend("/topic/game-progress/" + game.getGameId(), game);
    return ResponseEntity.ok(game);
    }


  //end point to reset the game 
  @PostMapping("/reset")
  public ResponseEntity<String> reset(@RequestBody ConnectRequest request) throws NotFoundException {
    log.info("reset: {}, silentReset: {}", request.getGameId(), request.isSilentReset());
    Game game = gameService.resetGame(request.getGameId());
    if (!request.isSilentReset()) {
      simpMessagingTemplate.convertAndSend("/topic/game-alert/" + request.getGameId(), "A player has reset the game");
    }
    simpMessagingTemplate.convertAndSend("/topic/game-progress/" + game.getGameId(), game);
    

    return ResponseEntity.ok("Game reset succesfully");
  }
  

  // @GetMapping("/scores")
  // public ResponseEntity<Game> getGameScores(@RequestParam String gameId) throws NotFoundException {
  //   Game game = GameStorage.getInstance().getGames().get(gameId);
  // if (game == null) {
  //   throw new NotFoundException("Game not found with ID: " + gameId);
  // }

  
  // return ResponseEntity.ok(game);

  // }

  // endpoint to fetch the game score data only.
  @GetMapping("/scores")
  public ResponseEntity<Map<String, Integer>> getGameScores(@RequestParam String gameId) throws NotFoundException {
    Game game = GameStorage.getInstance().getGames().get(gameId);
    if (game == null) {
        throw new NotFoundException("Game not found with ID: " + gameId);
    }

    Map<String, Integer> response = new HashMap<>();
    response.put("player1Wins", game.getPlayer1Wins());
    response.put("player2Wins", game.getPlayer2Wins());
    response.put("ties", game.getTies());

    return ResponseEntity.ok(response);
}
}
