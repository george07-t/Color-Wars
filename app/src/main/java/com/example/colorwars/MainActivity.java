package com.example.colorwars;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import android.os.Handler;

import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int MAX_DOT = 4;
    private ImageButton[][] imageButtons = new ImageButton[5][5];
    private int[][] cellStates = new int[5][5]; // 0 for empty, 1 for red, 2 for blue
    private int[][] colorstatus = new int[5][5];
    private int[] imageResourcesRed = {R.drawable.r1, R.drawable.r2, R.drawable.r3, R.drawable.r4}; // Images for red circles
    private int[] imageResourcesBlue = {R.drawable.b1, R.drawable.b2, R.drawable.b3, R.drawable.b4}; // Images for blue circles
    private boolean redTurn = true; // Flag to track whose turn it is (true for red, false for blue)
    private static final long DELAY_TIME = 1000; // Delay time in milliseconds
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ImageButtons and set onClickListener
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                String buttonId = "id" + (i + 1) + (j + 1);
                int resId = getResources().getIdentifier(buttonId, "id", getPackageName());
                imageButtons[i][j] = findViewById(resId);
                imageButtons[i][j].setOnClickListener(this);
                cellStates[i][j] = 0; // Initialize all cells as empty
                colorstatus[i][j] = 0;
            }
        }
    }

    private boolean initialPhase1 = true;
    private boolean initialPhase2 = true;
    private boolean blues = false;
    private boolean reds = true;

    @Override
    public void onClick(View view) {
        // Check if the game is already over
        if (gameOver) return;

        // Get the row and column indices of the clicked ImageButton
        int[] indices = findButtonIndices(view);
        int rowIndex = indices[0];
        int colIndex = indices[1];

        if (blues) {
            if (initialPhase2 || colorstatus[rowIndex][colIndex] == 2) {
                // Blue player's turn
                int dotCount = Math.min(cellStates[rowIndex][colIndex] + 1, MAX_DOT); // Increment dot count
                int[] imageResources = imageResourcesBlue; // Choose image resources based on player color
                imageButtons[rowIndex][colIndex].setImageResource(imageResources[dotCount - 1]); // Set the image resource based on dot count
                cellStates[rowIndex][colIndex] = dotCount; // Update cell state

                // If the dot count is 4, spread again after a delay
                if (dotCount == 4) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            spreadCircles(rowIndex, colIndex, 2);

                        }
                    }, DELAY_TIME);
                }

                // Exit the initial phase after the first move
                initialPhase2 = false;
                colorstatus[rowIndex][colIndex] = 2;
            } else {
                // Ignore click if it's not the initial phase and the clicked box doesn't contain the blue dot
                Toast.makeText(this, "Invalid Move", Toast.LENGTH_SHORT).show();
                return; // Exit the method early
            }
        } else if (reds) {
            if (initialPhase1 || colorstatus[rowIndex][colIndex] == 1) {
                // Red player's turn
                int dotCount = Math.min(cellStates[rowIndex][colIndex] + 1, MAX_DOT); // Increment dot count
                int[] imageResources = imageResourcesRed; // Choose image resources based on player color
                imageButtons[rowIndex][colIndex].setImageResource(imageResources[dotCount - 1]); // Set the image resource based on dot count
                cellStates[rowIndex][colIndex] = dotCount; // Update cell state

                // If the dot count is 4, spread again after a delay
                if (dotCount == 4) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            spreadCircles(rowIndex, colIndex, 1);

                        }
                    }, DELAY_TIME);
                }
                // Exit the initial phase after the first move
                initialPhase1 = false;
                colorstatus[rowIndex][colIndex] = 1;
            } else {
                // Ignore click if it's not the initial phase and the clicked box doesn't contain the red dot
                Toast.makeText(this, "Invalid Move", Toast.LENGTH_SHORT).show();
                return; // Exit the method early
            }
        }

        // Switch turns to the other player
        blues = !blues;
        reds = !reds;


    }

    private int[] findButtonIndices(View view) {
        int[] indices = {-1, -1};
        // Get the row and column indices of the clicked ImageButton
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (view == imageButtons[i][j]) {
                    indices[0] = i;
                    indices[1] = j;
                    return indices;
                }
            }
        }
        return indices;
    }

    private Queue<int[]> spreadQueue = new LinkedList<>(); // Queue to hold positions for spreading

    private void spreadCircles(int rowIndex, int colIndex, int col) {
        // Define offsets for adjacent boxes (up, down, left, right)
        int[][] offsets = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        // Add the current position to the spread queue
        spreadQueue.offer(new int[]{rowIndex, colIndex});

        // Process the spread queue sequentially
        while (!spreadQueue.isEmpty()) {
            int[] position = spreadQueue.poll();
            int rows = position[0];
            int cols = position[1];

            // Spread to adjacent boxes
            for (int[] offset : offsets) {
                int newRow = rows + offset[0];
                int newCol = cols + offset[1];

                // Check if the new position is within bounds
                if (newRow >= 0 && newRow < 5 && newCol >= 0 && newCol < 5 && cellStates[newRow][newCol] != 4) {
                    if (col == 1) {
                        colorstatus[newRow][newCol] = 1;
                        // Increment the dot count of the adjacent box and update the image
                        cellStates[newRow][newCol] = (cellStates[newRow][newCol] + 1) % 5; // Increment dot count and ensure it stays within 1 to 4
                        int dotCount = cellStates[newRow][newCol] - 1; // Calculate dot count within valid range
                        imageButtons[newRow][newCol].setImageResource(imageResourcesRed[dotCount]);
                        // If the dot count of the adjacent box is 4, add it to the spread queue
                        if (dotCount == 3) {
                            spreadQueue.offer(new int[]{newRow, newCol});
                        }
                    } else if (col == 2) {
                        colorstatus[newRow][newCol] = 2;
                        // Increment the dot count of the adjacent box and update the image
                        cellStates[newRow][newCol] = (cellStates[newRow][newCol] + 1) % 5; // Increment dot count and ensure it stays within 1 to 4
                        int dotCount = cellStates[newRow][newCol] - 1; // Calculate dot count within valid range
                        imageButtons[newRow][newCol].setImageResource(imageResourcesBlue[dotCount]);
                        // If the dot count of the adjacent box is 4, add it to the spread queue
                        if (dotCount == 3) {
                            spreadQueue.offer(new int[]{newRow, newCol});
                        }
                    }
                }
            }
        }

        // After spreading is completed, remove the image of the box that has been spreaded
        imageButtons[rowIndex][colIndex].setImageResource(R.drawable.box1); // Remove the image
        cellStates[rowIndex][colIndex] = 0; // Update cell state
        colorstatus[rowIndex][colIndex] = 0; // Update colorstatus

        // Check for any remaining adjacent boxes with 4 dots and remove their images
        for (int[] offset : offsets) {
            int newRow = rowIndex + offset[0];
            int newCol = colIndex + offset[1];
            if (newRow >= 0 && newRow < 5 && newCol >= 0 && newCol < 5 && cellStates[newRow][newCol] == 4) {
                imageButtons[newRow][newCol].setImageResource(R.drawable.box1); // Remove the image
                cellStates[newRow][newCol] = 0; // Update cell state
                colorstatus[newRow][newCol] = 0; // Update colorstatus
            }
        }

        // Check for winner after spreading is completed and 4-dot circles are removed
        checkWinner();
    }




    private boolean gameOver = false;

    private boolean checkWinner() {

        int r = 0;
        int b = 0;
        // Check if there are any red or blue circles left on the board
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (colorstatus[i][j] == 1) {

                    r++;
                } else if (colorstatus[i][j] == 2) {

                    b++;
                }
            }
        }
        // Toast.makeText(this, "red=" + r + " blue=" + b, Toast.LENGTH_SHORT).show();
        // Display the winner if one of the players has no circles left
        if (r == 0) {
            gameOver = true;
            showWinnerDialog("Blue");
            return true;
        } else if (b == 0) {
            gameOver = true;
            showWinnerDialog("Red");
            return true;
        }
        return false;
    }

    private void showWinnerDialog(String winner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Over");
        builder.setMessage(winner + " Wins!");
        builder.setPositiveButton("Restart", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                gameOver = false;
                initialPhase1 = true;
                initialPhase2 = true;
                blues = false;
                reds = true;

                // Clear all circles from the board
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 5; j++) {
                        imageButtons[i][j].setImageResource(R.drawable.box1);
                        cellStates[i][j] = 0;
                        colorstatus[i][j] = 0;
                    }
                }
                dialog.dismiss();
            }
        });


        builder.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Dismiss the dialog if the player chooses to quit
            }
        });
        builder.setCancelable(false); // Prevent dismissing the dialog by tapping outside
        builder.show();
    }


}
