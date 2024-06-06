package test;

import robocode.*;
import robocode.util.Utils;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import hex.genmodel.MojoModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.exception.PredictException;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.prediction.*;
import impl.UIConfiguration;
import problemaB.GeneticAlgorithm;

public class Gorynych extends AdvancedRobot {
    private static final int NUM_ROWS = 4;
    private static final int NUM_COLS = 4;

    static final Random rand = new Random();

    private double sectionWidth;
    private double sectionHeight;

    private EasyPredictModelWrapper attackModel;
    private EasyPredictModelWrapper defenseModel;
    private boolean isAttackMode;

    /**
     * associar inimigos a retângulos e permitir remover retângulos de inimigos já
     * desatualizados
     */
    public static HashMap<String, Rectangle> inimigos;

    /**
     * lista de caminhos do mapa
     */
    public static List<GeneticAlgorithm.Point> path;

    /**
     * lista de pontos visitados no mapa
     */
    public static List<GeneticAlgorithm.Point> visitedPoints = new ArrayList<>();

    /**
     * contém o ponto atual para o qual o robot se está a dirigir
     */
    private int currentIndex = 0;

    /**
     * configurações
     */
    public static UIConfiguration conf;

    /**
     * lista de obstaculos preenchida ao fazer scan
     */
    public static List<Rectangle> obstacles = new ArrayList<>();

    private GeneticAlgorithm ga;
    private GeneticAlgorithm.Point targetPoint;

    @Override
    public void run() {
        File dataDirectory = this.getDataDirectory();

        // responde: vou acertar?
        String attackModelPath = dataDirectory.getAbsolutePath() + "\\DRF_Attack.zip";
        String defenseModelPath = dataDirectory.getAbsolutePath() +  "\\DRF_Defense.zip";
        
        try {
            attackModel = new EasyPredictModelWrapper(MojoModel.load(attackModelPath));
            defenseModel = new EasyPredictModelWrapper(MojoModel.load(defenseModelPath));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        sectionWidth = getBattleFieldWidth() / NUM_COLS;
        sectionHeight = getBattleFieldHeight() / NUM_ROWS;

        setColors(Color.red, Color.black, Color.yellow); // Set robot colors

        obstacles = new ArrayList<>();
        inimigos = new HashMap<>();
        conf = new UIConfiguration((int) getBattleFieldWidth(), (int) getBattleFieldHeight(), obstacles); // tamanho
        
        // Gera o caminho usando o algoritmo genético
        ga = new GeneticAlgorithm(conf);

        targetPoint = new GeneticAlgorithm.Point(rand.nextDouble() * conf.getWidth(), rand.nextDouble() * conf.getHeight());

        // Start in attack mode
        isAttackMode = true;
        while (true) {
            // only get a new point if the 
            // if (!isAttackMode) {
            //     sectionToMoveIn = getBestSectionToBe();
            // }

            // // generate a random point inside the target section
            // int x = rand.nextInt((int) (sectionWidth * sectionToMoveIn), (int) (sectionWidth * (sectionToMoveIn + 1)));
            // int y = rand.nextInt((int) (sectionWidth * sectionToMoveIn), (int) (sectionWidth * (sectionToMoveIn + 1)));

            // set the target point to be used on the GA and move
            moveAlongPath();

            execute(); // Yield control back to Robocode engine
        }
    }

    public int getBestSectionToBe() {
        if (defenseModel == null) {
            System.err.println("Model is not loaded properly.");
            return 0;
        }

        try {
            RowData inputData = null;
            double bestPrediction = 0;
            int bestSection = 0;

            for (int col  = 0 ; col < NUM_COLS; col++) {
                for (int row  = 0 ; row < NUM_ROWS; row++) {
                    inputData = new RowData();

                    // AbstractPrediction prediction = defenseModel.predict(inputData);
                    BinomialModelPrediction prediction = defenseModel.predictBinomial(inputData);

                    if (prediction.classProbabilities[0] > bestPrediction) {
                        bestPrediction = prediction.classProbabilities[0];
                        bestSection = col * NUM_COLS + row;
                    }
                }
            }
            
            System.out.println("ENEMY SECTION/POINT: " + bestSection + " - " + bestPrediction);

            return bestSection;

            // // Depending on the type of prediction returned by your model, handle it
            // // accordingly
            // // For example, if it's a classification model, you might return a label index;
            // // if it's a regression model, you might return a predicted value.
            // // Adjust this part according to your model's output.
            // // Below is just a placeholder code.
            // if (prediction instanceof BinomialModelPrediction) {
            //     BinomialModelPrediction binomialPrediction = (BinomialModelPrediction) prediction;
            //     return binomialPrediction.labelIndex; // Placeholder action for classification model
            // } else if (prediction instanceof RegressionModelPrediction) {
            //     RegressionModelPrediction regressionPrediction = (RegressionModelPrediction) prediction;
            //     return regressionPrediction.value; // Placeholder action for regression model
            // } else {
            //     return 0; // Default action in case of prediction failure
            // }
        } catch (PredictException e) {
            e.printStackTrace();
        }

        return 0;
    }
    
    /**
     * Método para mover o robô ao longo do caminho.
     */
    private void moveAlongPath() {
        if (path != null && !path.isEmpty() && currentIndex < path.size()) {
            GeneticAlgorithm.Point target = path.get(currentIndex);
            goTo(target.getX(), target.getY());
            visitedPoints.add(new GeneticAlgorithm.Point(getX(), getY())); // Adiciona o ponto visitado
            currentIndex++;
        } else {
            // Se chegou ao fim do caminho, recalcula o caminho (opcional)
            GeneticAlgorithm.Point startPoint = new GeneticAlgorithm.Point(this.getY(), this.getX());
            GeneticAlgorithm.Point endPoint = new GeneticAlgorithm.Point(targetPoint.getX(), targetPoint.getY());

            ga = new GeneticAlgorithm(conf);
            path = ga.findPath(startPoint, endPoint);

            currentIndex = 0;
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        int sectionToMoveIn = getBestSectionToBe();

        // generate a random point inside the target section
        double x = rand.nextDouble((int) (sectionWidth * sectionToMoveIn), (int) (sectionWidth * (sectionToMoveIn + 1)));
        double y = rand.nextDouble((int) (sectionWidth * sectionToMoveIn), (int) (sectionWidth * (sectionToMoveIn + 1)));

        targetPoint = new GeneticAlgorithm.Point(x, y);
    }

    public void onScannedRobot(ScannedRobotEvent event) {
        super.onScannedRobot(event);

        identifyEnemyPositions(event);
        fireDecision(event);
    }

    /**
     * Identify all the enemies and store them inside a map.
    */
    private void identifyEnemyPositions(ScannedRobotEvent event) {
        // Coordinates of the point where the enemy is in relation to the robot
        Point2D.Double enemyPoint = getCoordenadasInimigo(event.getBearing(), event.getDistance());
        enemyPoint.x -= this.getWidth() * 2.5 / 2; // Adjusts the position of the point relative to the robot size
        enemyPoint.y -= this.getHeight() * 2.5 / 2;

        // Create a rectangle around the point where the enemy was detected
        Rectangle enemyRect = new Rectangle((int) enemyPoint.x, (int) enemyPoint.y, (int) (this.getWidth() * 2.5),
                (int) (this.getHeight() * 2.5));

        // se já existe um retângulo para este inimigo, remove-o da lista de obstáculos
        if (inimigos.containsKey(event.getName())) {
            obstacles.remove(inimigos.get(event.getName()));
        }

        obstacles.add(enemyRect); // adiciona o retângulo representando a posição do inimigo à lista de obstáculos
        inimigos.put(event.getName(), enemyRect); // armazena o retângulo associado ao inimigo pelo nome do inimigo
    }

    /**
     * Decide if the robot should fire or not.
     */
    private void fireDecision(ScannedRobotEvent event) {
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
            BinomialModelPrediction p = attackModel.predictBinomial(row);
            System.out.println("Will I hit? -> " + p.label + "(" + p.classProbabilities[0] + "%)");

            // row.get("distance")

            // if the model predicts I will hit...
            if (p.classProbabilities[0] >= 0.80) {
                this.fire(3);
            } else if (p.classProbabilities[0] >= 0.20) {
                this.fire(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Calcula as coordenadas de um alvo com base no ângulo e na distância
     * fornecidos.
     * 
     * @param bearing   O ângulo para o alvo, em graus.
     * @param distancia A distância ao alvo.
     * @return As coordenadas do alvo.
     */
    private Double getCoordenadasInimigo(double bearing, double distancia) {
        // Converte o ângulo do robô e do alvo para radianos
        double angulo = Math.toRadians((getHeading() + bearing) % 360);

        // coordenadas do alvo com base no ângulo e na distância
        double x = getX() + Math.sin(angulo) * distancia;
        double y = getY() + Math.cos(angulo) * distancia;

        // Retorna as coordenadas do alvo
        return new Point2D.Double(x, y);
    }

    /**
     * robot go to END point/section (AG)
     */
    public void goTo(double x, double y) {
        double dx = x - getX();
        double dy = y - getY();
        double angleToTarget = Math.toDegrees(Math.atan2(dx, dy));
        double targetAngle = Utils.normalRelativeAngleDegrees(angleToTarget - getHeading());

        turnRight(targetAngle);
        ahead(Math.hypot(dx, dy));
    }

}