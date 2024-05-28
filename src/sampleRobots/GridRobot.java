package sampleRobots;

import robocode.*;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GridRobot extends AdvancedRobot {
    private static final int NUM_ROWS = 2;
    private static final int NUM_COLS = 2;
    private double sectionWidth;
    private double sectionHeight;
    private RobocodeFileOutputStream writer;
    private final long MAX_FILE_SIZE = 200000;

    @Override
    public void run() {
        try {
            writer = new RobocodeFileOutputStream(getDataFile("robot_data.csv"));
            String header = "time,robotX,robotY,enemyDistance,enemyBearing,hitByBullet\n";
            writer.write(header.getBytes());
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
        logData(enemyDistance, enemyBearing, 0);
        fire(1);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        logData(0, 0, 1);
        int currentSection = getSection(getX(), getY());
        int newSection = (currentSection + 1) % (NUM_ROWS * NUM_COLS);
        moveToSection(newSection);
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        // Back off a bit if the robot hits a wall
        back(20);
    }

    private void logData(double enemyDistance, double enemyBearing, int hitByBullet) {
        long fileSize = getDataFile("robot_data.csv").length();
        double robotX = getX();
        double robotY = getY();
        long time = getTime();

        if (fileSize == MAX_FILE_SIZE) {
            copyCSV();
            try {
                writer.flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        writeDataToFile(time, robotX, robotY, enemyDistance, enemyBearing, hitByBullet);
    }

    /**
     * writes data to file
     * 
     * @param time
     * @param robotX
     * @param robotY
     * @param enemyDistance
     * @param enemyBearing
     * @param hitByBullet
     */
    private void writeDataToFile(long time, double robotX, double robotY, double enemyDistance, double enemyBearing,
            int hitByBullet) {
        try {
            String data = String.format("%d,%.2f,%.2f,%.2f,%.2f,%d\n", time, robotX, robotY, enemyDistance,
                    enemyBearing, hitByBullet);
            writer.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWin(WinEvent event) {
        closeWriter();
    }

    @Override
    public void onDeath(DeathEvent event) {
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

    /**
     * copies one csv file to another 
     */
    private void copyCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader(getDataFile("robot_data.csv")));
                BufferedWriter writer = new BufferedWriter(new FileWriter(getDataFile("final_dataset.csv")))) {
    
            char[] buffer = new char[8192]; // Buffer size for reading and writing
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, charsRead);
            }
    
        } catch (IOException e) {
            // Handle IOException appropriately (e.g., log, display error message)
            e.printStackTrace();
        }
    }
    

}
