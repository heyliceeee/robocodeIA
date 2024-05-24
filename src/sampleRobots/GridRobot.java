package sampleRobots;

import robocode.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.awt.Color;

public class GridRobot extends AdvancedRobot {
    private static final int NUM_ROWS = 2;
    private static final int NUM_COLS = 2;
    private double sectionWidth;
    private double sectionHeight;
    private PrintWriter writer;

    @Override
    public void run() {
        setBodyColor(Color.red);
        setGunColor(Color.black);
        setRadarColor(Color.yellow);

        sectionWidth = getBattleFieldWidth() / NUM_COLS;
        sectionHeight = getBattleFieldHeight() / NUM_ROWS;

        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(getDataFile("robot_data.csv"), true)));
            writer.println("time,robotX,robotY,enemyDistance,enemyBearing,hitByBullet");
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            int section = getSection(getX(), getY());
            out.println("Current Section: " + section);

            // Example movement logic (you can adjust as needed)
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

    private void logData(double enemyDistance, double enemyBearing, int hitByBullet) {
        double robotX = getX();
        double robotY = getY();
        long time = getTime();

        try {
            writer.printf("%d,%.2f,%.2f,%.2f,%.2f,%d%n", time, robotX, robotY, enemyDistance, enemyBearing,
                    hitByBullet);
            writer.flush();
        } catch (Exception e) {
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
            writer.close();
        }
    }
}
