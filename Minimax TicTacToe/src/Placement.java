import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Placement {
    private char[] board;
    private char playerMove;
    private int dimension = 3;

    public Placement() {
        this.board = "         ".toCharArray();
        //Starting player
        this.playerMove = 'o';
    }

    public Placement(char[] board, char playerMove) {
        this.board = board;
        this.playerMove = playerMove;
    }

    public boolean gameOver() {
        Integer[] possible = checkPossibleMoves();
        return checkWin('x') || checkWin('o') || possible.length == 0;
    }


    public Placement place(int index) {
        //Copy board
        char[] clonedBoard = Arrays.copyOf(board, board.length);
        clonedBoard[index] = playerMove;

        //Manage player turns
        if (playerMove == 'x') {
            return new Placement(clonedBoard, 'o');
        } else
            return new Placement(clonedBoard, 'x');
    }

    public Integer[] checkPossibleMoves() {
        List<Integer> possible = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            if (board[i] == ' ') {
                possible.add(i);
            }
        }
        Integer[] arrayOfPossible = new Integer[possible.size()];
        possible.toArray(arrayOfPossible);
        //System.out.println("board " + possible);
        return arrayOfPossible;
    }

    public boolean checkWin(char playerMove) {
        for (int i = 0; i < dimension; i++) {
            //Check rows and columns
            if (checkBoard(playerMove, i * dimension, 1) || checkBoard(playerMove, i, dimension)) {
                return true;
            }
        }
        //Check diagonals
        if (checkBoard(playerMove, dimension - 1, dimension - 1) || checkBoard(playerMove, 0, dimension + 1)) {
            return true;
        }
        return false;

    }

    public boolean checkBoard(char playerMove, int start, int iter) {
        for (int i = 0; i < dimension; i++) {
            if (board[start + iter * i] != playerMove) {
                return false;
            }
        }
        return true;
    }

    public int minimax() {
        if (checkWin('x')) {
            return 10;
        }
        if (checkWin('o')) {
            return -10;
        }
        Integer[] possible = checkPossibleMoves();
        if (possible.length == 0) {
            return 0;
        }
        Integer minimaxValue = null;

        for (int i = 0; i < possible.length; i++) {
            Integer total = place(possible[i]).minimax();
            //System.out.println("Total: " + total);
            if (minimaxValue == null || playerMove == 'x' && minimaxValue < total || playerMove == 'o' && total < minimaxValue) {
                minimaxValue = total;
            }
        }

        //Depth to make the AI choose wiser moves
        if (playerMove == 'x') {
            return minimaxValue - 2;
        } else
            return minimaxValue + 1;
    }

    public int makeBestPlacement() {
        Integer minimaxValue = null;
        int bestMove = Integer.MIN_VALUE;
        Integer[] possible = checkPossibleMoves();
        for (int i = 0; i < possible.length; i++) {
            Integer total = place(possible[i]).minimax();
            if (minimaxValue == null || playerMove == 'x' && minimaxValue < total || playerMove == 'o' && total < minimaxValue) {
                minimaxValue = total;
                bestMove = possible[i];
            }
        }
        return bestMove;
    }


    public char[] getBoard() {
        return board;
    }

    public void setBoard(char[] board) {
        this.board = board;
    }

    public char getPlayerMove() {
        return playerMove;
    }

    public void setPlayerMove(char playerMove) {
        this.playerMove = playerMove;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }
}
