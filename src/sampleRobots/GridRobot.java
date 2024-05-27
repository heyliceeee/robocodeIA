package sampleRobots;

import robocode.*;
import java.io.*;
import java.awt.Color;

public class GridRobot extends AdvancedRobot {
    private static final int NUM_ROWS = 2;
    private static final int NUM_COLS = 2;
    private double sectionWidth;
    private double sectionHeight;
    private RobocodeFileOutputStream writer;
    private PrintWriter csvWriter;

    @Override
    public void run() {
        setBodyColor(Color.red);
        setGunColor(Color.black);
        setRadarColor(Color.yellow);

        sectionWidth = getBattleFieldWidth() / NUM_COLS;
        sectionHeight = getBattleFieldHeight() / NUM_ROWS;

        try {
            String header = "time,robotX,robotY,enemyDistance,enemyBearing,hitByBullet\n";
            writer = new RobocodeFileOutputStream(getDataFile("robot_dataset.csv"));
            csvWriter = new PrintWriter(new BufferedWriter(new FileWriter("grid_robot_dataset_full.csv", true)));
            writer.write(header.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            int section = getSection(getX(), getY());
            out.println("Current Section: " + section);

            moveToSection((section + 1) % (NUM_ROWS * NUM_COLS));
            execute();

            // Periodically flush and clear the intermediate file
            if (getTime() % 100 == 0) { // Adjust this value based on your requirements
                flushAndClearIntermediateFile();
            }
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
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
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
        back(20);
    }

    private void writeDataToFile(long time, double robotX, double robotY, double enemyDistance, double enemyBearing, int hitByBullet) {
        try {
            String data = String.format("%d,%.2f,%.2f,%.2f,%.2f,%d\n", time, robotX, robotY, enemyDistance, enemyBearing, hitByBullet);
            // writes data to the Robocode's intermediate file
            writer.write(data.getBytes());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logData(double enemyDistance, double enemyBearing, int hitByBullet) {
        double robotX = getX();
        double robotY = getY();
        long time = getTime();

        try {
            writeDataToFile(time, robotX, robotY, enemyDistance, enemyBearing, hitByBullet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWin(WinEvent event) {
        try {
            flushAndClearIntermediateFile();
            closeWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeath(DeathEvent event) {
        try {
            flushAndClearIntermediateFile();
            closeWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void flushAndClearIntermediateFile() {
        try {
            // Flush the Robocode file writer
            writer.flush();

            // Read data from the intermediate file
            File intermediateFile = getDataFile("robot_dataset.csv");
            BufferedReader reader = new BufferedReader(new FileReader(intermediateFile));
            String line;

            // Skip the header line
            reader.readLine();

            // Append the rest of the data to the full dataset file
            while ((line = reader.readLine()) != null) {
                csvWriter.append(line).append("\n");
            }

            // Close reader and clear the intermediate file
            reader.close();
            new FileWriter(intermediateFile).close(); // Clear the file content

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeWriter() throws IOException {
        if (writer != null) {
            writer.close();
        }
        if (csvWriter != null) {
            csvWriter.close();
        }
    }
}
