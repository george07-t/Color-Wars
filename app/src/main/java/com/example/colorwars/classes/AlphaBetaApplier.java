package com.example.colorwars.classes;

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
        instance.stoppedByTLE = false;
        return instance;
    }

    private AlphaBetaApplier() {
    }

    private boolean stoppedByTLE = false;
    private AtomicInteger botProgressInt = new AtomicInteger(0);

    public Pair<Integer, Integer> getBestMove(CellStatus[][] field, boolean maximizingPlayer) {
        N = field.length;
        DEPTH_LIMIT = predictDepthLimit(field);

        AtomicReference<Pair<Integer, Integer>> cellToPlace = new AtomicReference<>(null);
        AtomicInteger bestVal = new AtomicInteger(Integer.MIN_VALUE);

        for (int x = 0; x < N; x++) {
            for (int y = 0; y < N; y++) {
                if (field[x][y].getColor() == CellStatus.COLOR.NONE) {
                    // Simulate placing the move
                    CellStatus[][] newField = copyField(field);
                    newField[x][y].setColor(CellStatus.COLOR.BLUE);
                    newField[x][y].increaseDot();
                    int moveVal = applyAlphaBeta(newField, 0, !maximizingPlayer, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    if (stoppedByTLE) return null;

                    if (moveVal > bestVal.get()) {
                        cellToPlace.set(new Pair<>(x, y));
                        bestVal.set(moveVal);
                    } else if (moveVal == bestVal.get() && cellToPlace.get() != null) {
                        final Pair<Integer, Integer> prev = cellToPlace.get();

                        int prevVal = prev.getFirst() * N + prev.getSecond();
                        int curVal = x * N + y;

                        if (curVal < prevVal) {
                            cellToPlace.set(new Pair<>(x, y));
                        }
                    }

                    botProgressInt.incrementAndGet();
                }
            }
        }
        return cellToPlace.get();
    }

    private int applyAlphaBeta(CellStatus[][] field, int depth, boolean maximizingPlayer, int alpha, int beta) {
        if (depth == DEPTH_LIMIT) {
            return 0;
        }

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (field[i][j].getColor() == CellStatus.COLOR.NONE) {
                        CellStatus[][] newField = copyField(field);
                        newField[i][j].setColor(CellStatus.COLOR.BLUE);
                        newField[i][j].increaseDot();
                        int eval = applyAlphaBeta(newField, depth + 1, false, alpha, beta);
                        maxEval = Math.max(maxEval, eval);
                        alpha = Math.max(alpha, eval);
                        if (beta <= alpha) break;
                    }
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (field[i][j].getColor() == CellStatus.COLOR.NONE) {
                        CellStatus[][] newField = copyField(field);
                        newField[i][j].setColor(CellStatus.COLOR.RED);
                        newField[i][j].increaseDot();
                        int eval = applyAlphaBeta(newField, depth + 1, true, alpha, beta);
                        minEval = Math.min(minEval, eval);
                        beta = Math.min(beta, eval);
                        if (beta <= alpha) break;
                    }
                }
            }
            return minEval;
        }
    }

    private int predictDepthLimit(CellStatus[][] field) {
        int emptyCount = 0;
        for (CellStatus[] col : field) {
            for (CellStatus item : col) {
                if (item.getColor() == CellStatus.COLOR.NONE) emptyCount++;
            }
        }

        int emptyPercent = (100 * emptyCount) / (N * N);

        if (emptyPercent > 70) return N / 2;
        if (emptyPercent > 50) return N + 1;
        if (emptyPercent > 30) return (N * N) / 2;
        return N * N;
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
}
