package com.example.colorwars.classes;
import kotlin.Pair;

public class FuzzyLogicApplier {

    private static FuzzyLogicApplier instance = null;

    public static FuzzyLogicApplier getInstance() {
        if (instance == null) {
            instance = new FuzzyLogicApplier();
        }
        return instance;
    }

    private FuzzyLogicApplier() {
    }

    public Pair<Integer, Integer> getBestMove(CellStatus[][] field) {
        int N = field.length;
        Pair<Integer, Integer> bestMove = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int x = 0; x < N; x++) {
            for (int y = 0; y < N; y++) {
                if (field[x][y].getColor() != CellStatus.COLOR.BLUE) continue;

                double score = evaluateMove(field, x, y);

                if (score > bestScore) {
                    bestScore = score;
                    bestMove = new Pair<>(x, y);
                }
            }
        }

        return bestMove;
    }

    private double evaluateMove(CellStatus[][] field, int x, int y) {
        int dotCount = field[x][y].getDotCount();
        double dotCountMembership = getDotCountMembership(dotCount);

        double distanceMembership = getDistanceMembership(field, x, y);
        double spreadPotentialMembership = getSpreadPotentialMembership(field, x, y);

        // Fuzzy rules evaluation
        double score = Math.min(dotCountMembership, Math.min(distanceMembership, spreadPotentialMembership));

        return score;
    }

    private double getDotCountMembership(int dotCount) {
        // Fuzzy membership function for dot count
        if (dotCount <= 1) return 0.2;
        if (dotCount == 2) return 0.5;
        if (dotCount >= 3) return 1.0;
        return 0.0;
    }

    private double getDistanceMembership(CellStatus[][] field, int x, int y) {
        // Fuzzy membership function for distance to opponent cells
        int N = field.length;
        double minDistance = Double.POSITIVE_INFINITY;

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (field[i][j].getColor() == CellStatus.COLOR.RED) {
                    double distance = Math.sqrt(Math.pow(x - i, 2) + Math.pow(y - j, 2));
                    if (distance < minDistance) {
                        minDistance = distance;
                    }
                }
            }
        }

        if (minDistance <= 1) return 1.0;
        if (minDistance <= 2) return 0.5;
        return 0.2;
    }

    private double getSpreadPotentialMembership(CellStatus[][] field, int x, int y) {
        // Fuzzy membership function for spread potential
        CellStatus cell = field[x][y];
        if (cell.shouldSpread()) return 1.0;
        return 0.2;
    }
}

