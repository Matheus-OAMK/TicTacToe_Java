var turns = [
  ['#', '#', '#'],
  ['#', '#', '#'],
  ['#', '#', '#'],
];

var turn = 'X';
var gameOn = false;

function playerTurn(turn, id) {
  if (gameOn) {
    var cellBox = $('#' + id);
    if (!cellBox.hasClass('x') && !cellBox.hasClass('o')) {
      makeAMove(playerType, id.split('_')[0], id.split('_')[1]);
    }
  }
}

function displayResponse(data) {
  if (isGameReset(data)) {
    reset();
  }
  if (playerType === 'X') {
    $('[data-text=opponentName]').text(`Playing vs ${data.player2.login}`);
  } else {
    $('[data-text=opponentName]').text(`Playing vs ${data.player1.login}`);
  }


  
  updateTurnIcon(data);
  let board = data.board;
  for (let i = 0; i < board.length; i++) {
    for (let j = 0; j < board.length; j++) {
      if (board[i][j] === 1) {
        turns[i][j] = 'x';
      } else if (board[i][j] === 2) {
        turns[i][j] = 'o';
      }
      let id = i + '_' + j;
      $('#' + id).addClass(turns[i][j]);
    }
  }
  if (data.winner != null) {
    // alert(`The winner is ${data.winner}`);
    openModal(data.winner);
    fetchScores();
    gameOn = false;
    // reset();
  } else if (data.tie) {
    // alert(`Its a tie`);
    gameOn = false;
    openModal('tie');
    fetchScores();
  } else {
    gameOn = true;
  }
}

$('.tic').click(function () {
  // console.log('clicking tic');
  var slot = $(this).attr('id');
  playerTurn(turn, slot);
});

function reset() {
  turns = [
    ['#', '#', '#'],
    ['#', '#', '#'],
    ['#', '#', '#'],
  ];
  // $('.tic').text('#');
  $('.tic').removeClass('x o');
  // closeModal();
}

function isGameReset(data) {
  // Check if all cells in the board are 0 (or whatever represents an empty cell in your backend)
  let isBoardEmpty = data.board.every((row) => row.every((cell) => cell === 0));

  return isBoardEmpty;
}
// function to render the score of the game
function displayScores(data) {
  if (playerType === 'X') {
    $('#yourScore').text(`${data.player1Wins}`);
    $('#opponentScore').text(`${data.player2Wins}`);
    $('[data-text=player]').text(`X (YOU)`);
    $('[data-text=opponent]').text(`O (P2)`);
  } else {
    $('[data-text=player]').text(`O (YOU)`);
    $('[data-text=opponent]').text(`X (P2)`);
    $('#yourScore').text(`${data.player2Wins}`);
    $('#opponentScore').text(`${data.player1Wins}`);
  }
  $('#ties').text(`${data.ties}`);
}

function updateTurnIcon(data) {
  const SVG_PATHS = {
    X: './images/icon-x-default.svg#icon-x-default',
    O: './images/icon-o-default.svg#icon-o-default',
  };

  if (data.currentPlayer.login == data.player1.login) {
    document
      .querySelector('.gameplay__turn-icon use')
      .setAttribute('xlink:href', SVG_PATHS['X']);
  } else {
    document
      .querySelector('.gameplay__turn-icon use')
      .setAttribute('xlink:href', SVG_PATHS['O']);
  }
}

function openModal(type) {
  if (type === 'restart') {
    $('.heading-lg').text('Restart Game');
    $('[data-type=restart]').removeClass('hide');
    $('[data-type=winner]').addClass('hide');

    // here we remove class hide from the type1 buttons and add hide to type2 buttons
  } else if (type === 'tie') {
    $('.heading-lg').text(`The game ended in a tie`);
    $('[data-type=restart]').addClass('hide');
    $('[data-type=winner]').removeClass('hide');
  } else {
    // Here we remove hide from type2 buttons and hide the type1 buttons
    $('.heading-lg').text(`${type} takes the round`);
    $('[data-type=restart]').addClass('hide');
    $('[data-type=winner]').removeClass('hide');
  }
  $('.modal').removeClass('hide');
  $('.backdrop').removeClass('hide');
}

function closeModal() {
  $('.modal').addClass('hide');
  $('.backdrop').addClass('hide');
}

function quitGame() {
  location.reload();
}

// Function to render game or menu view
function renderView(view) {
  if (view == 'game') {
    $('[data-view=game]').removeClass('hide');
    $('[data-view=menu]').addClass('hide');
    $('[data-text=gameId]').text(`Game ID = ${gameId}`);
  } else if (view == 'menu') {
    $('[data-view=game]').addClass('hide');
    $('[data-view=menu]').removeClass('hide');
  }
}
