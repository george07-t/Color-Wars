package com.example.colorwars.classes;

import static com.example.colorwars.MainActivity.MAX_COLUMNS;
import static com.example.colorwars.MainActivity.MAX_ROWS;
import static com.example.colorwars.classes.CellStatus.COLOR.BLUE;
import static com.example.colorwars.classes.CellStatus.COLOR.RED;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import kotlin.Pair;

public class GeneticApplier {

    // Remember, the whole thing is running inside a thread
    private final Random random = new Random();
    private static final int POPULATION_SIZE = 4;
    private static final int MUTATION_RATE = 2;
    private static final int NO_OF_IT = 10;
    private static final int TOURNAMENT_SIZE = 2;
    private static GeneticApplier instance = null;

    private int N;
    private CellStatus[][] board;

    public static GeneticApplier getInstance(){
        if(instance == null) instance = new GeneticApplier();
        return instance;
    }

    private GeneticApplier() {}


    // Random point swapped
    private CellStatus[][] mutateChildren(CellStatus[][] chromosome){

        final int rand = random.nextInt(100);

        if (rand < MUTATION_RATE) {
            final int x = random.nextInt(chromosome.length);
            final int y = random.nextInt(chromosome.length);

            if(chromosome[x][y].shouldSpread()) spreadCell(chromosome, chromosome[x][y]);
        }

        return chromosome;
    }


    @NonNull
    private List<CellStatus[][]> applyCrossover(@NonNull CellStatus[][] parentOne, CellStatus[][] parentTwo, int N){

        final List<Pair<Integer,Integer>> valids = new ArrayList<>();
        for(int i=0; i<N; i++) {
            for (int j = 0; j < N; j++) {
                if (parentOne[i][j].getColor() != CellStatus.COLOR.NONE)
                    valids.add(new Pair<>(i, j));
                if (parentTwo[i][j].getColor() != CellStatus.COLOR.NONE)
                    valids.add(new Pair<>(i, j));
            }
        }

        final Random random = new Random();
        final int pairOne = random.nextInt(valids.size());
        final int pairTwo = random.nextInt(valids.size());

        Pair<Integer,Integer> indexOne = valids.get(pairOne);
        Pair<Integer,Integer> indexTwo = valids.get(pairTwo);


        final int x1 = indexOne.getFirst();
        final int y1 = indexOne.getSecond();

        final int x2 = indexTwo.getFirst();
        final int y2 = indexTwo.getFirst();

        final CellStatus[][] childOne = copyField(parentOne);
        final CellStatus[][] childTwo = copyField(parentTwo);

        childOne[x1][y1].increaseDot();
        childTwo[x2][y2].increaseDot();

        if(childOne[x1][y1].shouldSpread()) spreadCell(childOne, childOne[x1][y1]);
        if(childTwo[x1][y1].shouldSpread()) spreadCell(childTwo, childTwo[x1][y1]);


        List<CellStatus[][]> list = new ArrayList<>();
        list.add(childOne);
        list.add(childTwo);

        return list;
    }


    private CellStatus[][] getParentFromTournament(List<CellStatus[][]> population){
        List<CellStatus[][]> tournament = new ArrayList<>();

        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            int randomIndex = random.nextInt(population.size());
            tournament.add(population.get(randomIndex));
        }

        return Collections.max(tournament, (chOne, chTwo) -> calcFitness(chTwo) - calcFitness(chOne));
    }

    private Pair<Integer,Integer> getTheBest(List<CellStatus[][]> population) {

        final int score = calcFitness( Collections.max(population, (chOne, chTwo) -> calcFitness(chTwo) - calcFitness(chOne)) );

        int xx = 0, yy = 0;

        for (int x = 0; x < N; x++) {
            for (int y = 0; y < N; y++) {
                if (board[x][y].getColor() == CellStatus.COLOR.BLUE) {
                    xx = x;
                    yy = y;
                    final CellStatus[][] copied = copyField(board);
                    copied[x][y].increaseDot();
                    if (copied[x][y].shouldSpread()) spreadCell(copied, copied[x][y]);

                    final int tempScore = calcFitness(copied);

                    if(tempScore > score) return new Pair<>(x,y);
                }

            }
        }

        return new Pair<>(xx,yy);
    }


    private int calcFitness(CellStatus[][] field){
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


    private List<CellStatus[][]> initPopulation(CellStatus[][] board){

        final List<Pair<Integer,Integer>> blueCells = new ArrayList<>();

        for(int x = 0; x < N; x++){
            for(int y = 0; y<N; y++){
                if(board[x][y].getColor() == CellStatus.COLOR.BLUE){
                    blueCells.add(new Pair<>(x,y));
                }
            }
        }

        final List<CellStatus[][]> populations = new ArrayList<>();

        for(int i=0; i<POPULATION_SIZE; i++){
            final int index = new Random().nextInt(blueCells.size());

            final int x = blueCells.get(index).getFirst();
            final int y = blueCells.get(index).getSecond();

            final CellStatus[][] chromosome = copyField(board);
            chromosome[x][y].increaseDot();
            if(chromosome[x][y].shouldSpread()) spreadCell(chromosome, chromosome[x][y]);

            populations.add(chromosome);
        }

        return populations;
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



    public Pair<Integer,Integer> predict(int N, CellStatus[][] board){
        this.N = N;
        this.board = board;

        final List<CellStatus[][]> populations = initPopulation(board);

        int it = 0;
        while (it++ < NO_OF_IT){

            final List<CellStatus[][]> offspring = new ArrayList<>();

            while (offspring.size() < populations.size()){

                final CellStatus[][] parentOne = getParentFromTournament(populations);
                final CellStatus[][] parentTwo = getParentFromTournament(populations);

                final List<CellStatus[][]> childAfterCross = applyCrossover(parentOne, parentTwo, N);

                for(CellStatus[][] child : childAfterCross) {
                    final CellStatus[][] mutatedChild = mutateChildren(child);
                    offspring.add(mutatedChild);
                }
            }

        }

        return getTheBest(populations);


    }

}
