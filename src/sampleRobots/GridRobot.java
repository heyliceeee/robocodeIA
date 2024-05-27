package sampleRobots;

import robocode.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.awt.Color;
import java.io.FileReader;

public class GridRobot extends AdvancedRobot {
    private static final int NUM_ROWS = 2;
    private static final int NUM_COLS = 2;
    private double sectionWidth;
    private double sectionHeight;
    private RobocodeFileOutputStream writer;
    private FileWriter csvWriter;

    @Override
    public void run() {
        setBodyColor(Color.red);
        setGunColor(Color.black);
        setRadarColor(Color.yellow);

        sectionWidth = getBattleFieldWidth() / NUM_COLS;
        sectionHeight = getBattleFieldHeight() / NUM_ROWS;

        try {
            String header = "time,robotX,robotY,enemyDistance,enemyBearing,hitByBullet\n";
            csvWriter = new FileWriter("grid_robot_dataset.csv");
            writer = new RobocodeFileOutputStream(getDataFile("robot_data.csv"));
            csvWriter.append(header);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            int section = getSection(getX(), getY());
            out.println("Current Section: " + section);

            moveToSection((section + 1) % (NUM_ROWS * NUM_COLS));
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
        back(20);
    }

    private void writeDataToFile(long time, double robotX, double robotY, double enemyDistance, double enemyBearing,
            int hitByBullet) {
        try {
            String dados = String.format("%d,%.2f,%.2f,%.2f,%.2f,%d\n", time, robotX, robotY, enemyDistance, enemyBearing, hitByBullet);
            // writes data do robocodes csv
            writer.write(dados.getBytes());
            // writes data to second csv
            csvWriter.append(dados);
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
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWin(WinEvent event) {
        try {
            closeWriter();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onDeath(DeathEvent event) {
        try {
            closeWriter();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void closeWriter() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }
}
