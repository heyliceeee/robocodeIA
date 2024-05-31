package sampleRobots;

import robocode.*;

import java.awt.geom.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import hex.genmodel.MojoModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.prediction.*;
import utils.Utils;

/**
 * This Robot uses the model provided to guess whether it will hit or miss an
 * enemy. This is a very basic model, trained specifically on the following
 * enemies: Corners, Crazy, SittingDuck, Walls. It is not expected to do
 * great...
 */
public class IntelligentGridRobot extends AdvancedRobot {

    EasyPredictModelWrapper model;

    @Override
    public void run() {
        super.run();

        System.out.println("Reading model from folder: " + getDataDirectory());
        try {
            //load the model
            //TODO: be sure to change the path to the model!
            //you will need to crate the corresponding .data folder in the package of your robot's class, and copy the model there
            model = new EasyPredictModelWrapper(MojoModel.load("C:\\Users\\Utilizador\\Documents\\NetBeansProjects\\TP_IA_2024_Resources_Quim\\build\\classes\\sampleRobots\\IntelligentGridRobot.data\\DP_GridRobot.zip"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        while (true) {
            //setAhead(100);
            //setTurnLeft(100);
            this.setTurnRadarRight(360);
            Random rand = new Random();
            setAllColors(new Color(rand.nextInt(3), rand.nextInt(3), rand.nextInt(3)));
            execute();
        }

    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        super.onScannedRobot(event);

        Point2D.Double coordinates = utils.Utils.getEnemyCoordinates(this, event.getBearing(), event.getDistance());
        System.out.println("Enemy " + event.getName() + " spotted at " + coordinates.x + "," + coordinates.y + "\n");

        RowData row = new RowData();
        //row.put("time", event.getTime());
        row.put("robotX", coordinates.x);
        row.put("robotY", coordinates.y);
        row.put("enemyDistance", event.getDistance());
        row.put("enemyBearing", event.getBearing());

        try {
            BinomialModelPrediction p = model.predictBinomial(row);
            System.out.println("Will I hit? ->" + p.label);

            //if the model predicts I will hit...
            if (p.label.equals(0)) {
                goTo((double)row.get("robotX"), (double)row.get("robotY"));
             
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Método para normalizar o ângulo de direção entre -180 e 180 graus
    double normalizeBearing(double angle) {
        while (angle > 180) {
            angle -= 360;
        }
        while (angle < -180) {
            angle += 360;
        }
        return angle;
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
}
