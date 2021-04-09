package com.server.connect5.component;

import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class GameBoard {

    public static final int ROW_LENGTH = 6;
    public static final int COL_LENGTH = 9;

    public static final int WINNING_SCORE = 5;

    public char[][] currentBoard = new char[ROW_LENGTH][COL_LENGTH];

    public GameBoard() {
        clearBoard();
    }

    /**
     * Clears the board by changing every entry to a blank space
     */
    public void clearBoard() {
        for (char[] r : this.currentBoard)
            Arrays.fill(r, ' ');
    }

    /**
     * Prints the gameboard in a neat format
     *
     * @return formatted board state as string
     */
    public String printBoard() {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < this.currentBoard.length; i++) {
            for (int j = 0; j < this.currentBoard[i].length; j++) {
                sb.append("[" + this.currentBoard[i][j] + "]");
            }
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }
}

