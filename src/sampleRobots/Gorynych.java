package sampleRobots;

import java.awt.Color;
import java.io.IOException;

import hex.genmodel.MojoModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.exception.PredictException;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.prediction.*;
import interf.Attacker;
import interf.Defender;

public class Gorynych extends GeneticAlgorithmBot implements Attacker, Defender {
    private EasyPredictModelWrapper attackModel;
    private EasyPredictModelWrapper defenseModel;
    private boolean isAttackMode;

    @Override
    public void run() {
        try {
            attackModel = new EasyPredictModelWrapper(MojoModel.load(getDataFile("DRF_Attack.zip").getAbsolutePath()));
            //defenseModel = new EasyPredictModelWrapper(
              //      MojoModel.load(getDataFile("DRF_Defense.zip").getAbsolutePath()));
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
        isAttackMode = false; // Switch to defense mode
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        isAttackMode = true; // Switch to attack mode
    }

    private double[] getFeatures() {
        return new double[] { getX(), getY() };
    }

    private double predictAction(EasyPredictModelWrapper model, double[] features) {
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
