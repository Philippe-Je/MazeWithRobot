package org.example.mazewithrobot;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.*;

public class Robot {
    /** The ImageView representing the robot in the UI. */
    private ImageView robotView;

    /** The current x-coordinate of the robot. */
    private double x;

    /** The current y-coordinate of the robot. */
    private double y;

    /** The image of the maze. */
    private Image mazeImage;

    /** Indicates whether the robot is currently solving the maze. */
    private boolean isSolving;

    /** The size of each step the robot takes. */
    private static final int STEP_SIZE = 10;

    /** The speed at which the robot solves the maze (in milliseconds). */
    private static final int SOLVE_SPEED = 100;

    /** The size of the robot image. */
    private static final int ROBOT_SIZE = 20;

    /** The path the robot has taken, stored as a stack of Points. */
    private Stack<Point> path;

    /** The set of points the robot has visited to avoid revisiting. */
    private Set<Point> visited;

    /** The color of the path in the maze. */
    private Color pathColor;

    /** The starting point of the maze. */
    private Point startPoint;

    /** The exit point of the maze. */
    private Point exitPoint;

    /**
     * Constructs a new Robot object.
     *
     * @param robotView The ImageView representing the robot in the UI.
     * @param mazeImage The image of the maze.
     */
    public Robot(ImageView robotView, Image mazeImage) {
        this.robotView = robotView;
        this.x = robotView.getX();
        this.y = robotView.getY();
        this.mazeImage = mazeImage;
        this.isSolving = false;
        this.path = new Stack<>();
        this.visited = new HashSet<>();
        this.pathColor = getPathColor();
        findEntranceAndExit();
    }

    /**
     * Finds the entrance and exit points of the maze.
     * This method scans the borders of the maze image to locate the start and exit points.
     */
    private void findEntranceAndExit() {
        PixelReader pixelReader = mazeImage.getPixelReader();
        int width = (int) mazeImage.getWidth();
        int height = (int) mazeImage.getHeight();

        // Check top and bottom borders
        for (int x = 0; x < width; x++) {
            if (pixelReader.getColor(x, 0).equals(pathColor)) {
                if (startPoint == null) startPoint = new Point(x, 0);
                else exitPoint = new Point(x, 0);
            }
            if (pixelReader.getColor(x, height - 1).equals(pathColor)) {
                if (startPoint == null) startPoint = new Point(x, height - 1);
                else exitPoint = new Point(x, height - 1);
            }
        }

        // Check left and right borders
        for (int y = 0; y < height; y++) {
            if (pixelReader.getColor(0, y).equals(pathColor)) {
                if (startPoint == null) startPoint = new Point(0, y);
                else exitPoint = new Point(0, y);
            }
            if (pixelReader.getColor(width - 1, y).equals(pathColor)) {
                if (startPoint == null) startPoint = new Point(width - 1, y);
                else exitPoint = new Point(width - 1, y);
            }
        }
    }

    /**
     * Moves the robot by the specified delta values.
     * This method is used for manual control of the robot.
     *
     * @param deltaX The change in x-coordinate.
     * @param deltaY The change in y-coordinate.
     */
    public void move(int deltaX, int deltaY) {
        double newX = x + deltaX;
        double newY = y + deltaY;

        if (isValidMove(newX, newY)) {
            x = newX;
            y = newY;
            updateRobotPosition();
        }
    }

    /**
     * Starts the maze-solving algorithm.
     * This method initiates the process of automatically solving the maze.
     */
    public void solveMaze() {
        if (isSolving) return;
        isSolving = true;

        path.push(new Point(x, y));
        visited.add(new Point(x, y));

        // Create a timeline for step-by-step maze solving
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(SOLVE_SPEED), e -> {
            if (!isAtExit()) {
                step();
            } else {
                isSolving = false;
                ((Timeline)e.getSource()).stop();
                System.out.println("Exit reached!");
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Performs a single step in the maze-solving algorithm.
     * This method is called repeatedly to navigate the maze.
     */
    private void step() {
        if (path.isEmpty()) {
            isSolving = false;
            return;
        }

        Point current = path.peek();
        List<Point> neighbors = getUnvisitedNeighbors(current);

        if (!neighbors.isEmpty()) {
            Point next = neighbors.get(0);
            path.push(next);
            visited.add(next);
            moveTo(next);
        } else {
            path.pop();
            if (!path.isEmpty()) {
                moveTo(path.peek());
            }
        }
    }

    /**
     * Gets the unvisited neighbors of a given point.
     * This method checks all four directions around a point for valid, unvisited locations.
     *
     * @param p The point to check for neighbors.
     * @return A list of unvisited neighboring points.
     */
    private List<Point> getUnvisitedNeighbors(Point p) {
        List<Point> neighbors = new ArrayList<>();
        int[][] directions = {{0, -STEP_SIZE}, {STEP_SIZE, 0}, {0, STEP_SIZE}, {-STEP_SIZE, 0}};

        for (int[] dir : directions) {
            Point neighbor = new Point(p.x + dir[0], p.y + dir[1]);
            if (isValidMove(neighbor.x, neighbor.y) && !visited.contains(neighbor)) {
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }

    /**
     * Moves the robot to a specific point.
     * This method updates the robot's position and refreshes its view.
     *
     * @param p The point to move to.
     */
    private void moveTo(Point p) {
        x = p.x;
        y = p.y;
        updateRobotPosition();
    }

    /**
     * Checks if the robot has reached the exit.
     * This method compares the robot's current position with the known exit point.
     *
     * @return true if the robot is at the exit, false otherwise.
     */
    private boolean isAtExit() {
        return Math.abs(x - exitPoint.x) < STEP_SIZE && Math.abs(y - exitPoint.y) < STEP_SIZE;
    }

    /**
     * Checks if a move to the specified coordinates is valid.
     * This method ensures the robot stays within the maze boundaries and on the path.
     *
     * @param newX The new x-coordinate.
     * @param newY The new y-coordinate.
     * @return true if the move is valid, false otherwise.
     */
    private boolean isValidMove(double newX, double newY) {
        if (newX < 0 || newX > mazeImage.getWidth() - ROBOT_SIZE ||
                newY < 0 || newY > mazeImage.getHeight() - ROBOT_SIZE) {
            return false;
        }

        return isPathAvailable(newX, newY) &&
                isPathAvailable(newX + ROBOT_SIZE, newY) &&
                isPathAvailable(newX, newY + ROBOT_SIZE) &&
                isPathAvailable(newX + ROBOT_SIZE, newY + ROBOT_SIZE);
    }

    /**
     * Checks if a path is available at the specified coordinates.
     * This method reads the color of the pixel at the given coordinates to determine if it's a path.
     *
     * @param x The x-coordinate to check.
     * @param y The y-coordinate to check.
     * @return true if a path is available, false otherwise.
     */
    private boolean isPathAvailable(double x, double y) {
        PixelReader pixelReader = mazeImage.getPixelReader();

        if (x < 0 || x >= mazeImage.getWidth() || y < 0 || y >= mazeImage.getHeight()) {
            return false;
        }

        Color color = pixelReader.getColor((int) x, (int) y);
        return color.equals(pathColor);
    }

    /**
     * Gets the color of the path in the maze.
     * This method reads the color of the pixel at the robot's initial position.
     *
     * @return The Color object representing the path color.
     */
    private Color getPathColor() {
        PixelReader pixelReader = mazeImage.getPixelReader();
        return pixelReader.getColor((int) x, (int) y);
    }

    /**
     * Updates the robot's position in the UI.
     * This method synchronizes the robot's logical position with its visual representation.
     */
    private void updateRobotPosition() {
        robotView.setX(x);
        robotView.setY(y);
    }

    /**
     * Gets the current x-coordinate of the robot.
     *
     * @return The current x-coordinate.
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the current y-coordinate of the robot.
     *
     * @return The current y-coordinate.
     */
    public double getY() {
        return y;
    }

    /**
     * Represents a point in the maze.
     * This inner class is used to store coordinates and provide equality comparisons.
     */
    private static class Point {
        double x, y;

        /**
         * Constructs a new Point object.
         *
         * @param x The x-coordinate of the point.
         * @param y The y-coordinate of the point.
         */
        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Checks if this Point is equal to another object.
         *
         * @param o The object to compare with.
         * @return true if the objects are equal, false otherwise.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return Double.compare(point.x, x) == 0 && Double.compare(point.y, y) == 0;
        }

        /**
         * Generates a hash code for this Point.
         *
         * @return The hash code value for this Point.
         */
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}