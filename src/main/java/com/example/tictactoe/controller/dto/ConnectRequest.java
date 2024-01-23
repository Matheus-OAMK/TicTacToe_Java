package com.example.tictactoe.controller.dto;

import com.example.tictactoe.model.Player;

import lombok.Data;

@Data
public class ConnectRequest {
  private Player player;
  private String gameId;
  private boolean silentReset = false;
}
