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
 * robo que dispara, dependendo do que o modelo prevê
 */
public class IntelligentRobot extends AdvancedRobot {
    String caminhoAlice = "D:\\githubProjects\\robocodeIA\\bin\\sampleRobots\\IntelligentRobot.data\\DRF_M1.zip";

    EasyPredictModelWrapper model;

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
            setAhead(100);
            setTurnLeft(100);

            Random rand = new Random();
            setAllColors(new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
            execute();
        }

    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        super.onScannedRobot(event);

        Point2D.Double coordinates = utils.Utils.getEnemyCoordinates(this, event.getBearing(), event.getDistance());
        System.out.println("Enemy " + event.getName() + " spotted at " + coordinates.x + "," + coordinates.y + "\n");

        RowData row = new RowData();
        // row.put("name", event.getName());
        row.put("distance", event.getDistance());
        row.put("velocity", event.getVelocity());
        row.put("angulo", event.getBearing());
        row.put("energia", event.getEnergy());
        row.put("cordenadaX", coordinates.x);
        row.put("cordenadaY", coordinates.y);
        // d.nome + "," + d.distancia + "," + d.velocidade + ","
        // +d.angulo+","+d.energia+","+ d.cordenadaX + "," + d.cordenadaY

        try {
            BinomialModelPrediction p = model.predictBinomial(row);
            System.out.println("Will I hit? ->" + p.label);
            System.out.println("Probability of hitting: " + p.classProbabilities[1]);

            // if the model predicts I will hit...
            if (p.classProbabilities[1] >= 0.20) {
                this.fire(3);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
