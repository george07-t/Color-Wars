package com.example.colorwars;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Handler;

import com.example.colorwars.classes.AlphaBetaApplier;
import com.example.colorwars.classes.CellStatus;
import com.example.colorwars.classes.FuzzyLogicApplier;
import com.example.colorwars.classes.GeneticApplier;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.colorwars.classes.CellStatus.COLOR.*;

import kotlin.Pair;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int MAX_ROWS = 5;
    public static final int MAX_COLUMNS = 5;
    private final ImageButton[][] imageButtons = new ImageButton[5][5];

    private boolean redTurn = true;
    private boolean initialPhaseBlue = true, initialPhaseRed = true;

    private static final long DELAY_TIME = 1000;
    //private final Handler handler = new Handler();
    private final CellStatus[][] cellStates = new CellStatus[5][5];
    private TextView rd, be;
    int redCount = 0, blueCount = 0;
    private final Random random = new Random();
    private AlphaBetaApplier alphaBetaApplier;
    private FuzzyLogicApplier fuzzyLogicApplier;
    private GeneticApplier geneticApplier;
    int whichbot = 0;
    private TextView algo;
    MediaPlayer mediaPlayer, botmediaplayer, win, loose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.setStatusBarColor(this.getResources().getColor(R.color.main));
        setContentView(R.layout.activity_main);
        algo = findViewById(R.id.algo);
        Intent intent = getIntent();
        String value = intent.getStringExtra("key");
        if (Objects.equals(value, "easy")) {
            whichbot = 1;
            algo.setText("Easy (Fuzzy)");
        } else if (Objects.equals(value, "medium")) {
            whichbot = 2;
            algo.setText("Medium (GA)");
        } else if (Objects.equals(value, "hard")) {
            whichbot = 3;
            algo.setText("Hard (AB)");
        }
        rd = findViewById(R.id.redid);
        be = findViewById(R.id.blueid);
        mediaPlayer = MediaPlayer.create(this, R.raw.popcell);
        botmediaplayer = MediaPlayer.create(this, R.raw.botpop);
        win = MediaPlayer.create(this, R.raw.win);
        loose = MediaPlayer.create(this, R.raw.loose);
        alphaBetaApplier = AlphaBetaApplier.getInstance();
        fuzzyLogicApplier = FuzzyLogicApplier.getInstance();
        geneticApplier = GeneticApplier.getInstance();
        for (int row = 0; row < MAX_ROWS; row++) {
            for (int col = 0; col < MAX_COLUMNS; col++) {
                String buttonId = "id" + (row + 1) + (col + 1);
                @SuppressLint("DiscouragedApi")
                int resId = getResources().getIdentifier(buttonId, "id", getPackageName());
                imageButtons[row][col] = findViewById(resId);
                imageButtons[row][col].setOnClickListener(this);
                cellStates[row][col] = new CellStatus(row, col); // initialize with blank cell
            }
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        for (int row = 0; row < MAX_ROWS; row++) {
            for (int col = 0; col < MAX_COLUMNS; col++) {
                imageButtons[row][col].setEnabled(enabled);
            }
        }
    }

    public void onClick(View view) {
        if (isGameOver) return;

        // Get the row and column indices of the clicked ImageButton
        int[] indices = findButtonIndices(view);
        int rowIndex = indices[0];
        int colIndex = indices[1];

        if (rowIndex == -1 || colIndex == -1) {
            showToast("Invalid cell clicked");
            return;
        }

        final CellStatus clickedCell = cellStates[rowIndex][colIndex];

        if (!initialPhaseBlue && !initialPhaseRed && clickedCell.isBlank()) {
            showToast("Can't click on empty cell");
            return;
        }

        if (initialPhaseRed) {
            clickedCell.setColor(RED);
            redCount++;
            rd.setText(String.valueOf(redCount));
            initialPhaseRed = false;
        } else if (initialPhaseBlue) {
            if (!clickedCell.isBlank()) {
                showToast("Invalid move");
                return;
            }
            clickedCell.setColor(BLUE);
            blueCount++;
            be.setText(String.valueOf(blueCount));
            initialPhaseBlue = false;
        } else if (!clickedCell.canClick(redTurn)) {
            showToast("Invalid move");
            return;
        }
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
        clickedCell.increaseDot();
        imageButtons[rowIndex][colIndex].setImageResource(clickedCell.getImage());

        setButtonsEnabled(false);

        if (clickedCell.shouldSpread()) {
            mHandler.postDelayed(() -> {
                if (clickedCell.shouldSpread()) {
                    spreadCell(clickedCell);
                }
                redTurn = !redTurn;
                if (!redTurn) {
                    mHandler.postDelayed(this::botMoveAndEnableButtons, DELAY_TIME);
                } else {
                    setButtonsEnabled(true);
                }
            }, DELAY_TIME);
        } else {
            redTurn = !redTurn;
            if (!redTurn) {
                mHandler.postDelayed(this::botMoveAndEnableButtons, DELAY_TIME);
            } else {
                setButtonsEnabled(true);
            }
        }
    }

    private void botMoveAndEnableButtons() {
        botMove();
        setButtonsEnabled(true);
    }


    private int[] findButtonIndices(View view) {
        for (int r = 0; r < MAX_ROWS; r++) {
            for (int c = 0; c < MAX_COLUMNS; c++) {
                if (view == imageButtons[r][c]) {
                    return new int[]{r, c};
                }
            }
        }
        return new int[]{-1, -1};
    }

    private final Queue<CellStatus> cellsToSpreadQueue = new LinkedList<>();

    private void spreadCell(CellStatus rootCell) {
        final int[][] offsets = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        cellsToSpreadQueue.offer(rootCell);
        CellStatus.COLOR rootColor = rootCell.getColor();

        while (!cellsToSpreadQueue.isEmpty()) {
            CellStatus currentCell = cellsToSpreadQueue.poll();
            if (currentCell == null) continue;

            for (int[] offset : offsets) {
                int row = currentCell.rowIndex + offset[0];
                int col = currentCell.colIndex + offset[1];

                if (row < 0 || row >= MAX_ROWS || col < 0 || col >= MAX_COLUMNS) continue;

                CellStatus adjCell = cellStates[row][col];

                adjCell.setColor(rootColor);
                adjCell.increaseDot();
                imageButtons[adjCell.rowIndex][adjCell.colIndex].setImageResource(adjCell.getImage());

                if (adjCell.shouldSpread()) {
                    cellsToSpreadQueue.offer(adjCell);
                }
            }
            // make current cell blank
            imageButtons[currentCell.rowIndex][currentCell.colIndex].setImageResource(currentCell.makeBlankAndGetImage());
        }

        CellStatus.COLOR winner = getWinner();
        isGameOver = (winner != NONE);
        if (isGameOver) {
            showWinnerDialog((winner == RED) ? "RED" : "BLUE");
        }
    }

    private Toast mToast = null;

    private void showToast(String message) {
        try {
            if (mToast != null) mToast.cancel();
            mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
            mToast.show();
        } catch (Exception ignored) {
        }
    }

    private boolean isGameOver = false;

    private CellStatus.COLOR getWinner() {
        redCount = 0;
        blueCount = 0;
        for (CellStatus[] rows : cellStates) {
            for (CellStatus cell : rows) {
                if (cell.getColor() == RED) redCount++;
                else if (cell.getColor() == BLUE) blueCount++;
            }
        }
        rd.setText(String.valueOf(redCount));
        be.setText(String.valueOf(blueCount));
        if (redCount == 0) return BLUE;
        if (blueCount == 0) return RED;
        return NONE;
    }

    private void showWinnerDialog(String winner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_winner, null);
        builder.setView(dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        Button btnPrimary = dialogView.findViewById(R.id.btnPrimary);
        Button btnSecondary = dialogView.findViewById(R.id.btnSecondary);

        tvTitle.setText("Game Over");
        tvMessage.setText(winner + " Wins!");

        if (Objects.equals(winner, "RED")) {
            win.start();
        } else {
            loose.start();
        }

        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);

        btnPrimary.setText("Restart");
        btnPrimary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGameOver = false;
                initialPhaseBlue = true;
                initialPhaseRed = true;
                redTurn = true;
                redCount = 0;
                blueCount = 0;
                rd.setText(String.valueOf(redCount));
                be.setText(String.valueOf(blueCount));
                for (int r = 0; r < MAX_ROWS; r++) {
                    for (int c = 0; c < MAX_COLUMNS; c++) {
                        imageButtons[r][c].setImageResource(cellStates[r][c].makeBlankAndGetImage());
                    }
                }
                alertDialog.dismiss();
            }
        });

        btnSecondary.setText("Quit");
        btnSecondary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }


    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean hasRedNeighbor(int row, int col, CellStatus[][] cellStates) {
        int[] rowOffsets = {-1, 0, 1, 0}; // Up, Right, Down, Left
        int[] colOffsets = {0, 1, 0, -1}; // Up, Right, Down, Left

        for (int i = 0; i < 4; i++) {
            int newRow = row + rowOffsets[i];
            int newCol = col + colOffsets[i];

            if (newRow >= 0 && newRow < cellStates.length && newCol >= 0 && newCol < cellStates[0].length) {
                if (cellStates[newRow][newCol].getColor() == RED) {
                    return true;
                }
            }
        }
        return false;
    }

    private void botMove() {
        service.submit(() -> {
            if (isGameOver) return;

            if (initialPhaseBlue) { // First move: Select any non-red cell
                final int[] rowIndex = new int[1], colIndex = new int[1];
                do {
                    rowIndex[0] = random.nextInt(MAX_ROWS);
                    colIndex[0] = random.nextInt(MAX_COLUMNS);

                } while (cellStates[rowIndex[0]][colIndex[0]].getColor() == RED || hasRedNeighbor(rowIndex[0], colIndex[0], cellStates));

                CellStatus clickedCell = cellStates[rowIndex[0]][colIndex[0]];
                clickedCell.setColor(BLUE);
                clickedCell.increaseDot();
                blueCount++;
                mHandler.post(() -> {
                    be.setText(String.valueOf(blueCount));
                    imageButtons[rowIndex[0]][colIndex[0]].setImageResource(clickedCell.getImage());
                    botmediaplayer.start();
                });
                initialPhaseBlue = false;
            } else {// Subsequent moves: Select only blue cells
                final CellStatus[][] field = new CellStatus[MAX_ROWS][MAX_COLUMNS];
                for (int i = 0; i < MAX_ROWS; i++) {
                    System.arraycopy(cellStates[i], 0, field[i], 0, MAX_COLUMNS);
                }
                final Pair<Integer, Integer> bestMove;
                if (whichbot == 1) {

                    bestMove = fuzzyLogicApplier.getBestMove(field);

                } else if (whichbot == 2) {
                    bestMove = geneticApplier.predict(field.length, field);
                } else {
                    bestMove = alphaBetaApplier.getBestMove(field, true);
                }

                if (bestMove != null) {
                    final int rowIndex = bestMove.getFirst();
                    final int colIndex = bestMove.getSecond();
                    CellStatus clickedCell = cellStates[rowIndex][colIndex];
                    if (clickedCell.getColor() == BLUE && clickedCell.canClick(false)) {
                        clickedCell.increaseDot();

                        System.out.println("Using bot move");

                        mHandler.post(() -> {
                            imageButtons[rowIndex][colIndex].setImageResource(clickedCell.getImage());
                            botmediaplayer.start();
                        });

                        if (clickedCell.shouldSpread()) {
                            mHandler.postDelayed(() -> {
                                if (clickedCell.shouldSpread()) {
                                    spreadCell(clickedCell);
                                }
                            }, DELAY_TIME);
                        }
                    }
                } else {
                    Toast.makeText(this, "Random", Toast.LENGTH_SHORT).show();
                    final int rowIndex = random.nextInt(MAX_ROWS);
                    final int colIndex = random.nextInt(MAX_COLUMNS);

                    CellStatus clickedCell = cellStates[rowIndex][colIndex];
                    if (clickedCell.getColor() == BLUE && clickedCell.canClick(false)) {
                        clickedCell.increaseDot();
                        mHandler.post(() -> {
                            imageButtons[rowIndex][colIndex].setImageResource(clickedCell.getImage());
                        });

                        if (clickedCell.shouldSpread()) {
                            mHandler.postDelayed(() -> spreadCell(clickedCell), DELAY_TIME);
                        }
                    }
                }

            }

            redTurn = !redTurn;
        });

    }

    @Override
    public void onBackPressed() {
        if (!isGameOver) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_winner, null);
            builder.setView(dialogView);

            TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
            TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
            Button btnPrimary = dialogView.findViewById(R.id.btnPrimary);
            Button btnSecondary = dialogView.findViewById(R.id.btnSecondary);

            tvTitle.setText("Exit");
            tvMessage.setText("Are you sure you want to exit the game?");

            AlertDialog alertDialog = builder.create();
            alertDialog.setCancelable(false);

            btnPrimary.setText("Yes");
            btnPrimary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.super.onBackPressed();
                }
            });

            btnSecondary.setText("No");
            btnSecondary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });

            alertDialog.show();
        }
    }

}
