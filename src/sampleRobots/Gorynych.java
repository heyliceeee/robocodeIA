package sampleRobots;

import robocode.*;
import java.awt.Color;
import java.io.IOException;
import java.util.Random;
import hex.genmodel.MojoModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.exception.PredictException;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.prediction.*;

public class Gorynych extends AdvancedRobot {
    private EasyPredictModelWrapper attackModel;
    private EasyPredictModelWrapper defenseModel;
    private boolean isAttackMode;
    
    String attackModelPath = "C:\\Users\\xavie\\Github\\robocodeIA\\bin\\sampleRobots\\Gorynych.data\\DRF_Attack.zip";
    String defenseModelPath = "C:\\Users\\xavie\\Github\\robocodeIA\\bin\\sampleRobots\\Gorynych.data\\DRF_Defense.zip";

    @Override
    public void run() {
        try {
            System.out.println("Loading attack model from: " + attackModelPath);
            attackModel = new EasyPredictModelWrapper(MojoModel.load(attackModelPath));
            System.out.println("Loading defense model from: " + defenseModelPath);
            defenseModel = new EasyPredictModelWrapper(MojoModel.load(defenseModelPath));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        setColors(Color.red, Color.blue, Color.green); // body, gun, radar
        isAttackMode = true; // Start in attack mode

        while (true) {
            if (isAttackMode) {
                attackMode();
            } else {
                defenseMode();
            }
        }
    }

    public void attackMode() {
        // Example of using attack model
        double[] features = getFeatures();
        double action = predictAction(attackModel, features);
        executeAction(action);
    }

    public void defenseMode() {
        // Example of using defense model
        double[] features = getFeatures();
        double action = predictAction(defenseModel, features);
        executeAction(action);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        isAttackMode = false;
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        isAttackMode = true;
    }

    private double[] getFeatures() {
        return new double[] { getX(), getY() };
    }

    private double predictAction(EasyPredictModelWrapper model, double[] features) {
        if (model == null) {
            System.err.println("Model is not loaded properly.");
            return 0;
        }

        try {
            RowData row = new RowData();
            row.put("robotX", features[0]);
            row.put("robotY", features[1]);

            AbstractPrediction prediction = model.predict(row);

            if (prediction instanceof BinomialModelPrediction) {
                BinomialModelPrediction binomialPrediction = (BinomialModelPrediction) prediction;
                return binomialPrediction.labelIndex;
            } else if (prediction instanceof RegressionModelPrediction) {
                RegressionModelPrediction regressionPrediction = (RegressionModelPrediction) prediction;
                return regressionPrediction.value;
            } else {
                return 0;
            }
        } catch (PredictException e) {
            e.printStackTrace();
            return 0; // Default action in case of prediction failure
        }
    }

    private void executeAction(double action) {
        // Implement action execution based on prediction
        if (action == 0) {
            setAhead(100);
        } else if (action == 1) {
            setTurnLeft(90);
        } else {
            setFire(1);
        }
        execute();
    }
}
