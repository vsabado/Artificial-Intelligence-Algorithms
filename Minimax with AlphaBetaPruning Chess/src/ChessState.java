import javax.sound.midi.Soundbank;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/// Represents the state of a chess game
class ChessState {
    public static final int MAX_PIECE_MOVES = 27;
    public static final int None = 0;
    public static final int Pawn = 1;
    public static final int Rook = 2;
    public static final int Knight = 3;
    public static final int Bishop = 4;
    public static final int Queen = 5;
    public static final int King = 6;
    public static final int PieceMask = 7;
    public static final int WhiteMask = 8;
    public static final int AllMask = 15;

    public static final int A = 0;
    public static final int B = 1;
    public static final int C = 2;
    public static final int D = 3;
    public static final int E = 4;
    public static final int F = 5;
    public static final int G = 6;
    public static final int H = 7;


    int[] m_rows;

    public static String getPieceName(int num) {
        switch (num) {
            case Pawn:
                return "Pawn";
            case Rook:
                return "Rook";
            case Knight:
                return "Knight";
            case Bishop:
                return "Bishop";
            case Queen:
                return "Queen";
            case King:
                return "King";
            default:
                return "";
        }
    }

    ChessState() {
        m_rows = new int[8];
        resetBoard();
    }

    ChessState(ChessState that) {
        m_rows = new int[8];
        for (int i = 0; i < 8; i++)
            this.m_rows[i] = that.m_rows[i];
    }

    int getPiece(int col, int row) {
        return (m_rows[row] >> (4 * col)) & PieceMask;
    }

    boolean isWhite(int col, int row) {
        return (((m_rows[row] >> (4 * col)) & WhiteMask) > 0 ? true : false);
    }

    /// Sets the piece at location (col, row). If piece is None, then it doesn't
    /// matter what the value of white is.
    void setPiece(int col, int row, int piece, boolean white) {
        m_rows[row] &= (~(AllMask << (4 * col)));
        m_rows[row] |= ((piece | (white ? WhiteMask : 0)) << (4 * col));
    }


    /// Sets up the board for a new game
    void resetBoard() {
        setPiece(0, 0, Rook, true);
        setPiece(1, 0, Knight, true);
        setPiece(2, 0, Bishop, true);
        setPiece(3, 0, Queen, true);
        setPiece(4, 0, King, true);
        setPiece(5, 0, Bishop, true);
        setPiece(6, 0, Knight, true);
        setPiece(7, 0, Rook, true);
        for (int i = 0; i < 8; i++)
            setPiece(i, 1, Pawn, true);
        for (int j = 2; j < 6; j++) {
            for (int i = 0; i < 8; i++)
                setPiece(i, j, None, false);
        }
        for (int i = 0; i < 8; i++)
            setPiece(i, 6, Pawn, false);
        setPiece(0, 7, Rook, false);
        setPiece(1, 7, Knight, false);
        setPiece(2, 7, Bishop, false);
        setPiece(3, 7, Queen, false);
        setPiece(4, 7, King, false);
        setPiece(5, 7, Bishop, false);
        setPiece(6, 7, Knight, false);
        setPiece(7, 7, Rook, false);
    }

    /// Positive means white is favored. Negative means black is favored.
    int heuristic(Random rand) {
        int score = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int p = getPiece(x, y);
                int value;
                switch (p) {
                    case None:
                        value = 0;
                        break;
                    case Pawn:
                        value = 10;
                        break;
                    case Rook:
                        value = 63;
                        break;
                    case Knight:
                        value = 31;
                        break;
                    case Bishop:
                        value = 36;
                        break;
                    case Queen:
                        value = 88;
                        break;
                    case King:
                        value = 500;
                        break;
                    default:
                        throw new RuntimeException("what?");
                }
                if (isWhite(x, y))
                    score += value;
                else
                    score -= value;
            }
        }
        return score + rand.nextInt(3) - 1;
    }

    /// Returns an iterator that iterates over all possible moves for the specified color
    ChessMoveIterator iterator(boolean white) {
        return new ChessMoveIterator(this, white);
    }

    /// Returns true iff the parameters represent a valid move
    boolean isValidMove(int xSrc, int ySrc, int xDest, int yDest) {
        ArrayList<Integer> possible_moves = moves(xSrc, ySrc);
        for (int i = 0; i < possible_moves.size(); i += 2) {
            if (possible_moves.get(i).intValue() == xDest && possible_moves.get(i + 1).intValue() == yDest)
                return true;
        }
        return false;
    }

    /// Print a representation of the board to the specified stream
    void printBoard(PrintStream stream) {
        stream.println("  A  B  C  D  E  F  G  H");
        stream.print(" +");
        for (int i = 0; i < 8; i++)
            stream.print("--+");
        stream.println();
        for (int j = 7; j >= 0; j--) {
            stream.print(Character.toString((char) (49 + j)));
            stream.print("|");
            for (int i = 0; i < 8; i++) {
                int p = getPiece(i, j);
                if (p != None) {
                    if (isWhite(i, j))
                        stream.print("w");
                    else
                        stream.print("b");
                }
                switch (p) {
                    case None:
                        stream.print("  ");
                        break;
                    case Pawn:
                        stream.print("p");
                        break;
                    case Rook:
                        stream.print("r");
                        break;
                    case Knight:
                        stream.print("n");
                        break;
                    case Bishop:
                        stream.print("b");
                        break;
                    case Queen:
                        stream.print("q");
                        break;
                    case King:
                        stream.print("K");
                        break;
                    default:
                        stream.print("?");
                        break;
                }
                stream.print("|");
            }
            stream.print(Character.toString((char) (49 + j)));
            stream.print("\n +");
            for (int i = 0; i < 8; i++)
                stream.print("--+");
            stream.println();
        }
        stream.println("  A  B  C  D  E  F  G  H");
    }

    /// Pass in the coordinates of a square with a piece on it
    /// and it will return the places that piece can move to.
    ArrayList<Integer> moves(int col, int row) {
        ArrayList<Integer> pOutMoves = new ArrayList<Integer>();
        int p = getPiece(col, row);
        boolean bWhite = isWhite(col, row);
        int nMoves = 0;
        int i, j;
        switch (p) {
            case Pawn:
                if (bWhite) {
                    if (!checkPawnMove(pOutMoves, col, inc(row), false, bWhite) && row == 1)
                        checkPawnMove(pOutMoves, col, inc(inc(row)), false, bWhite);
                    checkPawnMove(pOutMoves, inc(col), inc(row), true, bWhite);
                    checkPawnMove(pOutMoves, dec(col), inc(row), true, bWhite);
                } else {
                    if (!checkPawnMove(pOutMoves, col, dec(row), false, bWhite) && row == 6)
                        checkPawnMove(pOutMoves, col, dec(dec(row)), false, bWhite);
                    checkPawnMove(pOutMoves, inc(col), dec(row), true, bWhite);
                    checkPawnMove(pOutMoves, dec(col), dec(row), true, bWhite);
                }
                break;
            case Bishop:
                for (i = inc(col), j = inc(row); true; i = inc(i), j = inc(j))
                    if (checkMove(pOutMoves, i, j, bWhite))
                        break;
                for (i = dec(col), j = inc(row); true; i = dec(i), j = inc(j))
                    if (checkMove(pOutMoves, i, j, bWhite))
                        break;
                for (i = inc(col), j = dec(row); true; i = inc(i), j = dec(j))
                    if (checkMove(pOutMoves, i, j, bWhite))
                        break;
                for (i = dec(col), j = dec(row); true; i = dec(i), j = dec(j))
                    if (checkMove(pOutMoves, i, j, bWhite))
                        break;
                break;
            case Knight:
                checkMove(pOutMoves, inc(inc(col)), inc(row), bWhite);
                checkMove(pOutMoves, inc(col), inc(inc(row)), bWhite);
                checkMove(pOutMoves, dec(col), inc(inc(row)), bWhite);
                checkMove(pOutMoves, dec(dec(col)), inc(row), bWhite);
                checkMove(pOutMoves, dec(dec(col)), dec(row), bWhite);
                checkMove(pOutMoves, dec(col), dec(dec(row)), bWhite);
                checkMove(pOutMoves, inc(col), dec(dec(row)), bWhite);
                checkMove(pOutMoves, inc(inc(col)), dec(row), bWhite);
                break;
            case Rook:
                for (i = inc(col); true; i = inc(i))
                    if (checkMove(pOutMoves, i, row, bWhite))
                        break;
                for (i = dec(col); true; i = dec(i))
                    if (checkMove(pOutMoves, i, row, bWhite))
                        break;
                for (j = inc(row); true; j = inc(j))
                    if (checkMove(pOutMoves, col, j, bWhite))
                        break;
                for (j = dec(row); true; j = dec(j))
                    if (checkMove(pOutMoves, col, j, bWhite))
                        break;
                break;
            case Queen:
                for (i = inc(col); true; i = inc(i))
                    if (checkMove(pOutMoves, i, row, bWhite))
                        break;
                for (i = dec(col); true; i = dec(i))
                    if (checkMove(pOutMoves, i, row, bWhite))
                        break;
                for (j = inc(row); true; j = inc(j))
                    if (checkMove(pOutMoves, col, j, bWhite))
                        break;
                for (j = dec(row); true; j = dec(j))
                    if (checkMove(pOutMoves, col, j, bWhite))
                        break;
                for (i = inc(col), j = inc(row); true; i = inc(i), j = inc(j))
                    if (checkMove(pOutMoves, i, j, bWhite))
                        break;
                for (i = dec(col), j = inc(row); true; i = dec(i), j = inc(j))
                    if (checkMove(pOutMoves, i, j, bWhite))
                        break;
                for (i = inc(col), j = dec(row); true; i = inc(i), j = dec(j))
                    if (checkMove(pOutMoves, i, j, bWhite))
                        break;
                for (i = dec(col), j = dec(row); true; i = dec(i), j = dec(j))
                    if (checkMove(pOutMoves, i, j, bWhite))
                        break;
                break;
            case King:
                checkMove(pOutMoves, inc(col), row, bWhite);
                checkMove(pOutMoves, inc(col), inc(row), bWhite);
                checkMove(pOutMoves, col, inc(row), bWhite);
                checkMove(pOutMoves, dec(col), inc(row), bWhite);
                checkMove(pOutMoves, dec(col), row, bWhite);
                checkMove(pOutMoves, dec(col), dec(row), bWhite);
                checkMove(pOutMoves, col, dec(row), bWhite);
                checkMove(pOutMoves, inc(col), dec(row), bWhite);
                break;
            default:
                break;
        }
        return pOutMoves;
    }

    /// Moves the piece from (xSrc, ySrc) to (xDest, yDest). If this move
    /// gets a pawn across the board, it becomes a queen. If this move
    /// takes a king, then it will remove all pieces of the same color as
    /// the king that was taken and return true to indicate that the move
    /// ended the game.
    boolean move(int xSrc, int ySrc, int xDest, int yDest) {
        if (xSrc < 0 || xSrc >= 8 || ySrc < 0 || ySrc >= 8)
            throw new RuntimeException("out of range");
        if (xDest < 0 || xDest >= 8 || yDest < 0 || yDest >= 8)
            throw new RuntimeException("out of range");
        int target = getPiece(xDest, yDest);
        int p = getPiece(xSrc, ySrc);
        if (p == None)
            throw new RuntimeException("There is no piece in the source location");
        if (target != None && isWhite(xSrc, ySrc) == isWhite(xDest, yDest))
            throw new RuntimeException("It is illegal to take your own piece");
        if (p == Pawn && (yDest == 0 || yDest == 7))
            p = Queen; // a pawn that crosses the board becomes a queen
        boolean white = isWhite(xSrc, ySrc);
        setPiece(xDest, yDest, p, white);
        setPiece(xSrc, ySrc, None, true);
        if (target == King) {
            // If you take the opponent's king, remove all of the opponent's pieces. This
            // makes sure that look-ahead strategies don't try to look beyond the end of
            // the game (example: sacrifice a king for a king and some other piece.)
            int x, y;
            for (y = 0; y < 8; y++) {
                for (x = 0; x < 8; x++) {
                    if (getPiece(x, y) != None) {
                        if (isWhite(x, y) != white) {
                            setPiece(x, y, None, true);
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    static int inc(int pos) {
        if (pos < 0 || pos >= 7)
            return -1;
        return pos + 1;
    }

    static int dec(int pos) {
        if (pos < 1)
            return -1;
        return pos - 1;
    }

    boolean checkMove(ArrayList<Integer> pOutMoves, int col, int row, boolean bWhite) {
        if (col < 0 || row < 0)
            return true;
        int p = getPiece(col, row);
        if (p > 0 && isWhite(col, row) == bWhite)
            return true;
        pOutMoves.add(col);
        pOutMoves.add(row);
        return (p > 0);
    }

    boolean checkPawnMove(ArrayList<Integer> pOutMoves, int col, int row, boolean bDiagonal, boolean bWhite) {
        if (col < 0 || row < 0)
            return true;
        int p = getPiece(col, row);
        if (bDiagonal) {
            if (p == None || isWhite(col, row) == bWhite)
                return true;
        } else {
            if (p > 0)
                return true;
        }
        pOutMoves.add(col);
        pOutMoves.add(row);
        return (p > 0);
    }

    /// Represents a possible  move
    static class ChessMove {
        int xSource;
        int ySource;
        int xDest;
        int yDest;
    }

    /// Iterates through all the possible moves for the specified color.
    static class ChessMoveIterator {
        int x, y;
        ArrayList<Integer> moves;
        ChessState state;
        boolean white;

        /// Constructs a move iterator
        ChessMoveIterator(ChessState curState, boolean whiteMoves) {
            x = -1;
            y = 0;
            moves = null;
            state = curState;
            white = whiteMoves;
            advance();
        }

        private void advance() {
            if (moves != null && moves.size() >= 2) {
                moves.remove(moves.size() - 1);
                moves.remove(moves.size() - 1);
            }
            while (y < 8 && (moves == null || moves.size() < 2)) {
                if (++x >= 8) {
                    x = 0;
                    y++;
                }
                if (y < 8) {
                    if (state.getPiece(x, y) != ChessState.None && state.isWhite(x, y) == white) {
                        moves = state.moves(x, y);
                        //System.out.println("Moves: " + moves);
                    } else
                        moves = null;
                }
            }
        }

        /// Returns true iff there is another move to visit
        boolean hasNext() {
            return (moves != null && moves.size() >= 2);
        }

        /// Returns the next move
        ChessState.ChessMove next() {
            ChessState.ChessMove m = new ChessState.ChessMove();
            m.xSource = x;
            m.ySource = y;
            m.xDest = moves.get(moves.size() - 2);
            m.yDest = moves.get(moves.size() - 1);
            advance();
            return m;
        }
    }


    public int minimax(ChessState start, int depth, int alpha, int beta, boolean isWhite, Random rand) {
        //System.out.println("Depth: " + depth);
        if (depth == 0) {
            return start.heuristic(rand);
        }
        if (isWhite) {
            int maxEval = Integer.MIN_VALUE;
            ChessMoveIterator it = start.iterator(true);
            ChessState.ChessMove m;
            while (it.hasNext()) {
                ChessState child = new ChessState(start);
                m = it.next();
                child.move(m.xSource, m.ySource, m.xDest, m.yDest);
                //child.printBoard(System.out);
                int eval = minimax(child, depth - 1, alpha, beta, false, rand);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha)
                    break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            ChessMoveIterator it = start.iterator(false);
            ChessState.ChessMove m;
            while (it.hasNext()) {
                ChessState child = new ChessState(start);
                m = it.next();
                child.move(m.xSource, m.ySource, m.xDest, m.yDest);
                //child.printBoard(System.out);
                int eval = minimax(child, depth - 1, alpha, beta, true, rand);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha)
                    break;
            }
            return minEval;
        }
    }


    public ChessState.ChessMove makeBestMove(ChessState current, int depth, boolean isWhite, Random rand) {
        Integer minimaxValue = null;
        int bestMove = Integer.MIN_VALUE;
        ChessMoveIterator it = current.iterator(isWhite);
        ChessState.ChessMove m;
        ChessState.ChessMove best = null;
        List<ChessMove> moveList = new ArrayList<>();
        boolean endedGame = false;
        while (it.hasNext()) {
            ChessState child = new ChessState(current);
            m = it.next();
            endedGame = child.move(m.xSource, m.ySource, m.xDest, m.yDest);
            if (endedGame) {
                return m;
            }
            //child.printBoard(System.out);
            Integer total = minimax(child, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, !isWhite, rand);
            //System.out.println("Total:" + total);
            if (minimaxValue == null || (isWhite && total >= minimaxValue) || (!isWhite && minimaxValue >= total)) {
                if (total == minimaxValue) {
                    moveList.add(m);
                } else {
                    moveList.clear();
                    moveList.add(m);
                }
                minimaxValue = total;
            }
        }

        if (minimaxValue == Integer.MAX_VALUE || minimaxValue == Integer.MIN_VALUE) {
            System.out.println("Close to a checkmate!");
        } else
            System.out.println("Best move value was: " + minimaxValue);

        if (moveList.isEmpty()) {
            return null;
        }

        int randIndex = rand.nextInt(moveList.size());
        //Print piece and move
//        for (int i = 0; i < moveList.size(); i++) {
//            System.out.println(ChessState.getPieceName(getPiece(moveList.get(i).xSource, moveList.get(i).ySource)));
//            System.out.println("Destination x: " + moveList.get(i).xDest + " Destination y: " + moveList.get(i).yDest);
//        }
        //System.out.println(ChessState.getPieceName(getPiece(moveList.get(randIndex).xSource, moveList.get(randIndex).ySource)));
        //System.out.println("Destination x: " + moveList.get(randIndex).xDest + " Destination y: " + moveList.get(randIndex).yDest);

        return moveList.get(randIndex);
    }

    public void play(ChessState start, int bdepth, int wdepth, Random rand) {
        System.out.println("Black depth: " + bdepth);
        System.out.println("White depth: " + wdepth);
        start.printBoard(System.out);
        int loop = 0;
        boolean whitePlayerMoved = false;
        boolean blackPlayerMoved = true;
        boolean whiteAIMoved = false;
        boolean blackAIMoved = true;

        boolean endedGame = false;

        while (true) {
            if ((wdepth > 0 && blackPlayerMoved) || (wdepth > 0 && blackAIMoved)) {
                System.out.println("White's turn: ");
                ChessState.ChessMove best = start.makeBestMove(start, wdepth, true, rand);
                if (best == null) {
                    System.out.println("Draw");
                    break;
                }
                endedGame = start.move(best.xSource, best.ySource, best.xDest, best.yDest);
                whiteAIMoved = true;
                start.printBoard(System.out);

                if (endedGame) {
                    System.out.println("White Wins!");
                    break;
                }
            }

            if ((wdepth == 0 && blackPlayerMoved) || wdepth == 0 && blackAIMoved) {
                System.out.println("White's turn: ");
                char[] input = "".toCharArray();
                while (input.length != 4) {
                    input = getPlayerMoves();
                }
                if (isWhite(Character.getNumericValue(input[0]), Character.getNumericValue(input[1]))
                        && isValidMove(Character.getNumericValue(input[0]), Character.getNumericValue(input[1]),
                        Character.getNumericValue(input[2]), Character.getNumericValue(input[3]))) {

                    start.move(Character.getNumericValue(input[0]), Character.getNumericValue(input[1]),
                            Character.getNumericValue(input[2]), Character.getNumericValue(input[3]));
                    whitePlayerMoved = true;
                    start.printBoard(System.out);
                } else {
                    System.out.println("Either selected the wrong piece or made an invalid move!");
                    whitePlayerMoved = false;
                    whiteAIMoved = false;
                }

            }

            if ((bdepth == 0 && whiteAIMoved) || (bdepth == 0 && whitePlayerMoved)) {
                System.out.println("Black's turn: ");
                char[] input = "".toCharArray();
                while (input.length != 4) {
                    input = getPlayerMoves();
                }
                if (!isWhite(Character.getNumericValue(input[0]), Character.getNumericValue(input[1]))
                        && isValidMove(Character.getNumericValue(input[0]), Character.getNumericValue(input[1]), Character.getNumericValue(input[2]), Character.getNumericValue(input[3]))) {
                    start.move(Character.getNumericValue(input[0]), Character.getNumericValue(input[1]), Character.getNumericValue(input[2]), Character.getNumericValue(input[3]));
                    blackPlayerMoved = true;
                    start.printBoard(System.out);
                } else {
                    System.out.println("Either selected the wrong piece or made an invalid move!");
                    blackPlayerMoved = false;
                    blackAIMoved = false;
                }

            }

            if ((bdepth > 0 && whitePlayerMoved) || (bdepth > 0 && whiteAIMoved)) {
                System.out.println("Black's turn: ");
                ChessState.ChessMove best = start.makeBestMove(start, bdepth, false, rand);
                if (best == null) {
                    System.out.println("Draw");
                    break;
                }
                endedGame = start.move(best.xSource, best.ySource, best.xDest, best.yDest);
                blackAIMoved = true;
                start.printBoard(System.out);

                if (endedGame) {
                    System.out.println("Blacks Wins!");
                    break;
                }
            }
        }

    }

    public char[] getPlayerMoves() {
        System.out.println("Enter move or Q to quit");
        Scanner scan = new Scanner(System.in);
        String input = scan.nextLine();
        char[] inputArray = input.toUpperCase().toCharArray();

        for (int i = 0; i < inputArray.length; i++) {
            switch (inputArray[i]) {
                case 'A':
                    inputArray[i] = '0';
                    break;
                case 'B':
                    inputArray[i] = '1';
                    break;
                case 'C':
                    inputArray[i] = '2';
                    break;
                case 'D':
                    inputArray[i] = '3';
                    break;
                case 'E':
                    inputArray[i] = '4';
                    break;
                case 'F':
                    inputArray[i] = '5';
                    break;
                case 'G':
                    inputArray[i] = '6';
                    break;
                case 'H':
                    inputArray[i] = '7';
                    break;
                case 'Q': System.exit(0);
                default:
                    inputArray[i] = (char) (inputArray[i] - 1);
                    break;
            }

        }
        return inputArray;
    }

    public void clearBoard() {
        m_rows = new int[8];
        for (int j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++)
                setPiece(i, j, None, false);
        }
    }


    public static void main(String[] args) {
        ChessState s = new ChessState();
        s.resetBoard();
        Random rand = new Random();
        s.play(s, Integer.valueOf(args[0]), Integer.valueOf(args[1]), rand);
    }


//    public static void main(String[] args){
//        ChessState s = new ChessState();
//        s.testMinimax();
//    }

    public void printHeuristic() {
        System.out.println(this.heuristic(new Random()));
    }

    public void testMinimax() {
        Random rand = new Random();
        ChessState start = new ChessState();
        start.clearBoard();
        start.setPiece(D, 2, Queen, true);
        start.setPiece(E, 2, King, true);
        start.setPiece(D, 3, Pawn, false);
        start.setPiece(E, 3, Pawn, false);
        start.setPiece(F, 3, Pawn, false);
        start.setPiece(A, 7, King, false);
        start.printBoard(System.out);
        System.out.println("Heuristic: " + start.heuristic(rand));
        ChessState.ChessMove m = start.makeBestMove(start, 5, true, rand);
        start.move(m.xSource, m.ySource, m.xDest, m.yDest);
        start.printBoard(System.out);
        start.printHeuristic();
    }
}

