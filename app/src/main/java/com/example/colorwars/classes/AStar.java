package com.example.colorwars.classes;

import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashMap;

public class AStar {
    public static class Node {
        public int row, col, g, h, f;
        Node parent;

        public Node(int row, int col, int g, int h, Node parent) {
            this.row = row;
            this.col = col;
            this.g = g;
            this.h = h;
            this.f = g + h;
            this.parent = parent;
        }
    }

    public static ArrayList<Node> findPath(CellStatus[][] grid, int startRow, int startCol, int endRow, int endCol) {
        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingInt(node -> node.f));
        HashMap<String, Node> closedList = new HashMap<>();

        Node startNode = new Node(startRow, startCol, 0, Math.abs(startRow - endRow) + Math.abs(startCol - endCol), null);
        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();
            String key = currentNode.row + "," + currentNode.col;
            closedList.put(key, currentNode);

            if (currentNode.row == endRow && currentNode.col == endCol) {
                return constructPath(currentNode);
            }

            for (Node neighbor : getNeighbors(currentNode, grid)) {
                key = neighbor.row + "," + neighbor.col;
                if (closedList.containsKey(key)) {
                    continue;
                }
                neighbor.g = currentNode.g + 1;
                neighbor.h = Math.abs(neighbor.row - endRow) + Math.abs(neighbor.col - endCol);
                neighbor.f = neighbor.g + neighbor.h;
                openList.add(neighbor);
            }
        }
        return null;
    }

    private static ArrayList<Node> getNeighbors(Node node, CellStatus[][] grid) {
        ArrayList<Node> neighbors = new ArrayList<>();
        int[] rowOffsets = {-1, 1, 0, 0};
        int[] colOffsets = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            int newRow = node.row + rowOffsets[i];
            int newCol = node.col + colOffsets[i];
            if (isValidCell(newRow, newCol, grid)) {
                neighbors.add(new Node(newRow, newCol, 0, 0, node));
            }
        }
        return neighbors;
    }

    private static boolean isValidCell(int row, int col, CellStatus[][] grid) {
        return row >= 0 && col >= 0 && row < grid.length && col < grid[0].length && grid[row][col].getColor() != CellStatus.COLOR.RED;
    }

    private static ArrayList<Node> constructPath(Node node) {
        ArrayList<Node> path = new ArrayList<>();
        while (node != null) {
            path.add(node);
            node = node.parent;
        }
        return path;
    }
}
