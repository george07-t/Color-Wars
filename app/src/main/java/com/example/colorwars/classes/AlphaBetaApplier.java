package com.example.colorwars.classes;

import static com.example.colorwars.MainActivity.MAX_COLUMNS;
import static com.example.colorwars.MainActivity.MAX_ROWS;
import static com.example.colorwars.classes.CellStatus.COLOR.BLUE;
import static com.example.colorwars.classes.CellStatus.COLOR.RED;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import kotlin.Pair;

public class AlphaBetaApplier {

    private int N;
    private int DEPTH_LIMIT;

    private static AlphaBetaApplier instance = null;

    public static AlphaBetaApplier getInstance() {
        if (instance == null) {
            instance = new AlphaBetaApplier();
        }
        instance.botProgressInt.set(0);
        return instance;
    }

    private AlphaBetaApplier() {
    }

    private final AtomicInteger botProgressInt = new AtomicInteger(0);
    public Pair<Integer, Integer> getBestMove(CellStatus[][] field, boolean maximizingPlayer) {
        N = field.length;
        DEPTH_LIMIT = 2;

        AtomicReference<Pair<Integer, Integer>> cellToPlace = new AtomicReference<>(null);
        AtomicInteger bestVal = new AtomicInteger(Integer.MIN_VALUE);

        for (int x = 0; x < N; x++) {
            for (int y = 0; y < N; y++) {
                if (field[x][y].getColor() != BLUE) continue;

                // Simulate placing the move
                CellStatus[][] newField = copyField(field);
                newField[x][y].increaseDot();

                if(newField[x][y].shouldSpread()){
                    spreadCell(newField, newField[x][y]); // alert newField is modified inside the function
                }

                int moveVal = applyAlphaBeta(newField, 0, !maximizingPlayer, Integer.MIN_VALUE, Integer.MAX_VALUE);
                System.gc();

                if (moveVal >= bestVal.get()) {
                    cellToPlace.set(new Pair<>(x, y));
                    bestVal.set(moveVal);
                }

                botProgressInt.incrementAndGet();
            }
        }
        return cellToPlace.get();
    }

    private int evaluateBoard(CellStatus[][] field){
        final int[] blueDotCount = new int[4]; // x, 1,2,3,
        final int[] redDotCount = new int[4]; // x, 1,2,3,

        for(int i=0; i<N; i++){
            for(int j=0; j<N; j++){

                final CellStatus cell = field[i][j];
                if(cell.getColor() == RED){
                    redDotCount[cell.getDotCount()]++;
                }
                if(cell.getColor() == BLUE){
                    blueDotCount[cell.getDotCount()]++;
                }
            }
        }

        int score = 0;
        final int[] weights = new int[]{0,10,5,2};
        for(int i=0; i<blueDotCount.length; i++){
            score += (blueDotCount[i] - redDotCount[i]) * weights[i];
        }
        return score;
    }

    private int applyAlphaBeta(CellStatus[][] field, int depth, boolean maximizingPlayer, int alpha, int beta) {
        if (depth >= DEPTH_LIMIT) {
            return evaluateBoard(field);
        }

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;

            mainLoop:
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (field[i][j].getColor() != CellStatus.COLOR.BLUE) continue;

                    // Only click on blue cell
                    final CellStatus[][] newField = copyField(field);
                    newField[i][j].increaseDot();
                    spreadCell(newField,newField[i][j]);

                    int eval = applyAlphaBeta(newField, depth + 1, false, alpha, beta);
                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) break mainLoop;
                }
            }
            return maxEval;
        }
        else {
            int minEval = Integer.MAX_VALUE;
            mainLoop:
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (field[i][j].getColor() != RED) continue;

                    // Only click on red cells
                    final CellStatus[][] newField = copyField(field);
                    newField[i][j].increaseDot();
                    spreadCell(newField, newField[i][j]);

                    final int eval = applyAlphaBeta(newField, depth + 1, true, alpha, beta);
                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, eval);
                    if (beta <= alpha) break mainLoop;
                }
            }
            return minEval;
        }
    }

    private CellStatus[][] copyField(CellStatus[][] field) {
        CellStatus[][] newField = new CellStatus[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                newField[i][j] = new CellStatus(field[i][j].rowIndex, field[i][j].colIndex);
                newField[i][j].setColor(field[i][j].getColor());
                newField[i][j].setDotCount(field[i][j].getDotCount());
            }
        }
        return newField;
    }

    private void spreadCell(CellStatus[][] cellStates, CellStatus rootCell) {
        final int[][] offsets = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        final Queue<CellStatus> cellsToSpreadQueue = new LinkedList<>();

        cellsToSpreadQueue.add(rootCell);
        final CellStatus.COLOR rootColor = rootCell.getColor();

        while (!cellsToSpreadQueue.isEmpty()) {
            final CellStatus currentCell = cellsToSpreadQueue.poll();
            if (currentCell == null) continue;

            for (int[] offset : offsets) {
                int row = currentCell.rowIndex + offset[0];
                int col = currentCell.colIndex + offset[1];

                if (row < 0 || row >= MAX_ROWS || col < 0 || col >= MAX_COLUMNS) continue;

                final CellStatus adjCell = cellStates[row][col];

                adjCell.setColor(rootColor);
                adjCell.increaseDot();

                if (adjCell.shouldSpread()) {
                    cellsToSpreadQueue.offer(adjCell);
                }
            }
            currentCell.makeBlankAndGetImage();
        }
    }

}
