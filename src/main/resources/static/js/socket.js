const url = 'http://localhost:8080';
let stompClient;
let gameId;
let playerType;
let opponentName;

function connectToSocket(gameId, isGameCreator) {
  console.log('connecting to the game');
  let socket = new SockJS(url + '/gameplay');
  stompClient = Stomp.over(socket);
  stompClient.connect({}, function (frame) {
    console.log('connected to the frame ' + frame);

    if (isGameCreator) {
      stompClient.subscribe('/topic/game-join/' + gameId, function (message) {
        alert(message.body);
      });
    }

    stompClient.subscribe(
      '/topic/game-progress/' + gameId,
      function (response) {
        let data = JSON.parse(response.body);
        // console.log(data);
        displayResponse(data);
      }
    );
    stompClient.subscribe('/topic/game-alert/' + gameId, function (message) {
      alert(message.body);
    });
  });
}

function create_game() {
  let login = document.getElementById('login').value;
  if (login == null || login === '') {
    alert('Please enter a login name');
  } else {
    $.ajax({
      url: url + '/game/start',
      type: 'POST',
      dataType: 'json',
      contentType: 'application/json',
      data: JSON.stringify({
        login: login,
      }),
      success: function (data) {
        gameId = data.gameId;
        playerType = 'X';
        reset();
        connectToSocket(gameId, true);
        // alert('You created a game. Game id is : ' + data.gameId);
        renderView('game');
        gameOn = true;
        fetchScores();
      },
      error: function (error) {
        console.log(error);
      },
    });
  }
}

function connectToRandom() {
  let login = document.getElementById('login').value;
  if (login == null || login === '') {
    alert('Please enter a login name');
  } else {
    $.ajax({
      url: url + '/game/connect/random',
      type: 'POST',
      dataType: 'json',
      contentType: 'application/json',
      data: JSON.stringify({
        login: login,
      }),
      success: function (data) {
        gameId = data.gameId;
        playerType = 'O';
        opponentName = data.player1.login;
        reset();
        connectToSocket(gameId, false);
        alert(`You connected to ${data.player1.login}'s game`);
        renderView('game');
        displayResponse(data);
        fetchScores();
      },
      error: function (error) {
        console.log(error);
        let errorMessage = error.responseJSON.error;
        alert(errorMessage);
      },
    });
  }
}

function connectToSpecificGame() {
  let login = document.getElementById('login').value;
  if (login == null || login === '') {
    alert('Please enter a login name');
  } else {
    gameId = document.getElementById('game_id').value;
    if (gameId == null || gameId === '') {
      alert('Please enter a game ID');
    } else {
      $.ajax({
        url: url + '/game/connect',
        type: 'POST',
        dataType: 'json',
        contentType: 'application/json',
        data: JSON.stringify({
          player: {
            login: login,
          },
          gameId: gameId,
        }),
        success: function (data) {
          // console.log('Received data from /connect:', data);

          gameId = data.gameId;
          playerType = 'O';
          opponentName = data.player1;

          reset();
          connectToSocket(gameId, false);
          alert(`You connected to ${data.player1.login}'s game`);
          renderView('game');
          displayResponse(data);
          fetchScores();
        },
        error: function (error) {
          console.log(error);
          let errorMessage = error.responseJSON.error;
          alert(errorMessage);
        },
      });
    }
  }
}

function makeAMove(type, coordinateX, coordinateY) {
  // console.log(gameId)

  $.ajax({
    url: url + '/game/gameplay',
    type: 'POST',
    dataType: 'json',
    contentType: 'application/json',
    data: JSON.stringify({
      type: type,
      coordinateX: coordinateX,
      coordinateY: coordinateY,
      gameId: gameId,
    }),
    success: function (data) {
      gameOn = false;
      displayResponse(data);
    },
    error: function (error) {
      console.log(error);
      let errorMessage = error.responseJSON.error;
      alert(errorMessage);
    },
  });
}

function resetGame(silent = false) {
  $.ajax({
    url: url + '/game/reset',
    type: 'POST',
    dataType: 'text',
    contentType: 'application/json',
    data: JSON.stringify({
      gameId: gameId,
      silentReset: silent
    }),
    success: function (data) {
      // console.log('Received data from /reset:', data);
      // reset();
      fetchScores();
    },
    error: function (error) {
      console.log(error);
    },
  });
}

function fetchScores() {
  $.ajax({
    url: url + '/game/scores?gameId=' + gameId,
    type: 'GET',
    success: function (data) {
      displayScores(data);
      // console.log(data);
      // console.log("making fetch request" + data)
    },
    error: function (error) {
      console.log(error);
    },
  });
}
