package sampleRobots;

import robocode.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Robo that collects data for the localization problem dataset
 */
public class GridRobot extends AdvancedRobot {
    private static final int NUM_ROWS = 4;
    private static final int NUM_COLS = 4;
    private double sectionWidth;
    private double sectionHeight;
    private RobocodeFileOutputStream writer;

    private List<Rectangle> obstacles;
    private HashMap<String, Rectangle> enemies; // to associate enemies with rectangles
    private boolean hitByBullet;

    @Override
    public void run() {
        obstacles = new ArrayList<>();
        enemies = new HashMap<>();
        hitByBullet = false;

        List<String> existingData = new ArrayList<>();
        File dataFile = getDataFile("robot_data.csv");

        // Read existing data if the file exists
        if (dataFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    existingData.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            writer = new RobocodeFileOutputStream(dataFile);
            // If the file was empty, write the header
            if (existingData.isEmpty()) {
                String header = "time,robotX,robotY,enemyDistance,enemyBearing,hitByBullet,robotHeading,robotEnergy,enemyHeading,enemyVelocity,wallDistance,robotSection,enemySection\n";
                writer.write(header.getBytes());
            } else {
                // Rewrite existing data
                for (String line : existingData) {
                    writer.write((line + "\n").getBytes());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        setColors(Color.red, Color.black, Color.yellow); // Set robot colors

        sectionWidth = getBattleFieldWidth() / NUM_COLS;
        sectionHeight = getBattleFieldHeight() / NUM_ROWS;

        // Move to the center of the battlefield before starting
        goTo(getBattleFieldWidth() / 2, getBattleFieldHeight() / 2);

        while (true) {
            // Perform actions based on the robot's current section
            int currentSection = getSection(getX(), getY());
            out.println("Current Section: " + currentSection);

            // Example: move to the next section
            moveToSection((currentSection + 1) % (NUM_ROWS * NUM_COLS));

            execute();
        }
    }

    private int getSection(double x, double y) {
        int col = (int) (x / sectionWidth);
        int row = (int) (y / sectionHeight);
        return row * NUM_COLS + col;
    }

    private void moveToSection(int section) {
        int row = section / NUM_COLS;
        int col = section % NUM_COLS;
        double targetX = col * sectionWidth + sectionWidth / 2;
        double targetY = row * sectionHeight + sectionHeight / 2;
        goTo(targetX, targetY);
    }

    private void goTo(double x, double y) {
        double dx = x - getX();
        double dy = y - getY();
        double distance = Math.hypot(dx, dy);
        double angleToTarget = Math.toDegrees(Math.atan2(dx, dy));
        double angle = normalizeBearing(angleToTarget - getHeading());

        setTurnRight(angle);
        setAhead(distance);
    }

    private double normalizeBearing(double angle) {
        while (angle > 180)
            angle -= 360;
        while (angle < -180)
            angle += 360;
        return angle;
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        double enemyDistance = event.getDistance();
        double enemyBearing = event.getBearing();
        double enemyHeading = event.getHeading();
        double enemyVelocity = event.getVelocity();
        int robotSection = getSection(getX(), getY());

        Point2D.Double enemyPosition = getEnemyCoordinates(this, enemyBearing, enemyDistance);
        enemyPosition.x -= this.getWidth() * 2.5 / 2;
        enemyPosition.y -= this.getHeight() * 2.5 / 2;
        Rectangle enemyRect = new Rectangle((int) enemyPosition.x, (int) enemyPosition.y, (int) (this.getWidth() * 2.5),
                (int) (this.getHeight() * 2.5));

        if (enemies.containsKey(event.getName())) { // If the enemy already exists
            obstacles.remove(enemies.get(event.getName())); // Remove the old rectangle
        }

        obstacles.add(enemyRect);
        enemies.put(event.getName(), enemyRect);

        int enemySection = getSection(enemyPosition.x + this.getWidth() * 2.5 / 2,
                enemyPosition.y + this.getHeight() * 2.5 / 2);
        if (event.getEnergy() < getEnergy()) {
            hitByBullet = true;
        }

        logData(enemyDistance, enemyBearing, hitByBullet ? 1 : 0, enemyHeading, enemyVelocity, robotSection,
                enemySection);
        fire(1);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        hitByBullet = true;
        scanForEnemies();
        int currentSection = getSection(getX(), getY());
        int newSection = (currentSection + 1) % (NUM_ROWS * NUM_COLS);
        moveToSection(newSection);
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        // Back off a bit if the robot hits a wall and resume normal operation
        back(20);
        setTurnRight(90);
        ahead(100);
        scanForEnemies();
    }

    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        Rectangle rect = enemies.get(event.getName());
        obstacles.remove(rect);
        enemies.remove(event.getName());
    }

    @Override
    public void onDeath(DeathEvent event) {
        scanForEnemies();
        closeWriter();
    }

    private void logData(double enemyDistance, double enemyBearing, int hitByBullet, double enemyHeading,
            double enemyVelocity, int robotSection, int enemySection) {
        double robotX = getX();
        double robotY = getY();
        long time = getTime();
        double robotHeading = getHeading();
        double robotEnergy = getEnergy();
        double wallDistance = Math.min(Math.min(getX(), getBattleFieldWidth() - getX()),
                Math.min(getY(), getBattleFieldHeight() - getY()));

        writeDataToFile(time, robotX, robotY, enemyDistance, enemyBearing, hitByBullet, robotHeading, robotEnergy,
                enemyHeading, enemyVelocity, wallDistance, robotSection, enemySection);
    }

    private void writeDataToFile(long time, double robotX, double robotY, double enemyDistance, double enemyBearing,
            int hitByBullet, double robotHeading, double robotEnergy, double enemyHeading, double enemyVelocity,
            double wallDistance, int robotSection, int enemySection) {
        try {
            // Replace commas with dots in the double values
            String robotXStr = String.valueOf(robotX).replace(',', '.');
            String robotYStr = String.valueOf(robotY).replace(',', '.');
            String enemyDistanceStr = String.valueOf(enemyDistance).replace(',', '.');
            String enemyBearingStr = String.valueOf(enemyBearing).replace(',', '.');
            String robotHeadingStr = String.valueOf(robotHeading).replace(',', '.');
            String robotEnergyStr = String.valueOf(robotEnergy).replace(',', '.');
            String enemyHeadingStr = String.valueOf(enemyHeading).replace(',', '.');
            String enemyVelocityStr = String.valueOf(enemyVelocity).replace(',', '.');
            String wallDistanceStr = String.valueOf(wallDistance).replace(',', '.');

            // Construct the data string with replaced commas
            String data = String.format("%d,%s,%s,%s,%s,%d,%s,%s,%s,%s,%s,%d,%d\n", time, robotXStr, robotYStr,
                    enemyDistanceStr,
                    enemyBearingStr, hitByBullet, robotHeadingStr, robotEnergyStr, enemyHeadingStr, enemyVelocityStr,
                    wallDistanceStr, robotSection, enemySection);
            writer.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWin(WinEvent event) {
        closeWriter();
    }

    private void closeWriter() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Point2D.Double getEnemyCoordinates(Robot robot, double bearing, double distance) {
        double angle = Math.toRadians((robot.getHeading() + bearing) % 360);
        return new Point2D.Double((robot.getX() + Math.sin(angle) * distance),
                (robot.getY() + Math.cos(angle) * distance));
    }

    private void scanForEnemies() {
        // Scan for all enemies and log their data
        for (Rectangle enemyRect : enemies.values()) {
            double enemyX = enemyRect.getX() + enemyRect.getWidth() / 2;
            double enemyY = enemyRect.getY() + enemyRect.getHeight() / 2;
            double enemyDistance = Point2D.distance(getX(), getY(), enemyX, enemyY);
            double enemyBearing = Math.toDegrees(Math.atan2(enemyX - getX(), enemyY - getY()));
            int robotSection = getSection(getX(), getY());
            int enemySection = getSection(enemyX, enemyY);
            logData(enemyDistance, enemyBearing, 0, 0, 0, robotSection, enemySection);
        }
    }
}
