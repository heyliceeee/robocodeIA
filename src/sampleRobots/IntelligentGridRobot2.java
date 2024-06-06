package sampleRobots;

import robocode.*;

import java.awt.geom.*;
import java.awt.*;
import java.io.IOException;
import java.util.Random;
import hex.genmodel.MojoModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.prediction.*;

/**
 * Robo que vai para uma localização, dependendo do que o modelo prevê
 */
public class IntelligentGridRobot2 extends AdvancedRobot {

    EasyPredictModelWrapper model;
    String caminhoJoaquim = "";
    String caminhoDiogo = "";
    String caminhoAlice = "D:\\githubProjects\\robocodeIA\\bin\\sampleRobots\\IntelligentGridRobot2.data\\DP_GridRobot.zip";
    double lastX = Double.NaN;
    double lastY = Double.NaN;
    boolean isStuck = false;

    @Override
    public void run() {
        super.run();

        System.out.println("Reading model from folder: " + getDataDirectory());
        try {
            model = new EasyPredictModelWrapper(MojoModel.load(caminhoAlice));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        while (true) {
            this.setTurnRadarRight(360);

            Random rand = new Random();
            setAllColors(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        super.onScannedRobot(event);

        Point2D.Double coordinates = utils.Utils.getEnemyCoordinates(this, event.getBearing(), event.getDistance());
        System.out.println("Enemy " + event.getName() + " spotted at " + coordinates.x + "," + coordinates.y + "\n");

        RowData row = new RowData();
        row.put("robotX", coordinates.x); // coordenada x do robo inimigo
        row.put("robotY", coordinates.y); // coordenada y do robo inimigo
        row.put("enemyDistance", event.getDistance()); // distancia do inimigo em relacao ao robo
        row.put("enemyBearing", event.getBearing()); // angulo do inimigo em relacao ao robo

        double x = getX();
        double y = getY();

        try {
            BinomialModelPrediction p = model.predictBinomial(row);
            System.out.println("O inimigo irá me atingir? -> " + (p.label.equals("0") ? "não" : "sim"));

            if (p.label.equals("0")) // o inimigo nao me vai atingir
            {
                // ir para as coordenadas
                goTo((double) row.get("robotX"), (double) row.get("robotY"));
            } else // o inimigo vai me atingir
            {
                // Tome outra ação, como esquivar-se, atacar o inimigo ou recuar
                // Por exemplo, você pode adicionar uma lógica para esquivar-se da bala
                back(100);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        if (isNearWall(x, y)) {
            performRandomMovement();
            return;
        }

        double dx = x - getX();
        double dy = y - getY();
        double distance = Math.hypot(dx, dy);
        double angleToTarget = Math.toDegrees(Math.atan2(dx, dy));
        double angle = normalizeBearing(angleToTarget - getHeading());

        setTurnRight(angle);
        setAhead(distance);

        lastX = x;
        lastY = y;
    }

    private boolean isDifferentLocation(double x, double y) {
        return Double.isNaN(lastX) || Double.isNaN(lastY) || Math.hypot(lastX - x, lastY - y) > 50;
    }

    private void performEvasiveManeuver() {
        Random rand = new Random();
        double angle = rand.nextInt(90) - 45;
        setTurnRight(angle);
        setAhead(100);
    }

    private void performRandomMovement() {
        Random rand = new Random();
        double angle = rand.nextInt(360) - 180;
        setTurnRight(angle);
        setAhead(150);
    }

    private boolean isNearWall(double x, double y) {
        double margin = 50; // Distance from wall to consider as "near"
        return (x < margin || x > getBattleFieldWidth() - margin || y < margin || y > getBattleFieldHeight() - margin);
    }
}
