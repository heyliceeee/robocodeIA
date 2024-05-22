package sampleRobots;

import robocode.*;
import robocode.util.Utils;

import java.util.List;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.*;

import problemaB.GeneticAlgorithm;

import java.util.ArrayList;

public class GeneticAlgorithmBot extends AdvancedRobot {
    private List<GeneticAlgorithm.Point> path;
    private List<GeneticAlgorithm.Point> visitedPoints = new ArrayList<>();
    private int currentIndex = 0;
    private List<Rectangle> obstacles = new ArrayList<>();

    public void run() {
        // Configurações iniciais do robô
        setColors(Color.red, Color.blue, Color.green); // Corpo, canhão e radar
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        // Gera o caminho usando o algoritmo genético
        GeneticAlgorithm ga = new GeneticAlgorithm();
        path = ga.findPath();

        // Movimenta-se ao longo do caminho
        while (true) {
            moveAlongPath();
            execute(); // Executa as ações pendentes (movimento, rotação, etc.)
        }
    }

    // Método para mover o robô ao longo do caminho
    private void moveAlongPath() {
        if (currentIndex < path.size()) {
            GeneticAlgorithm.Point target = path.get(currentIndex);
            goTo(target.getX(), target.getY());
            visitedPoints.add(new GeneticAlgorithm.Point(getX(), getY())); // Adiciona o ponto visitado
            currentIndex++;
        } else {
            // Se chegou ao fim do caminho, recalcula o caminho (opcional)
            GeneticAlgorithm ga = new GeneticAlgorithm();
            path = ga.findPath();
            currentIndex = 0;
        }
    }

    // Método para mover o robô para um ponto específico
    private void goTo(double x, double y) {
        double dx = x - getX();
        double dy = y - getY();
        double angleToTarget = Math.toDegrees(Math.atan2(dx, dy));
        double targetAngle = Utils.normalRelativeAngleDegrees(angleToTarget - getHeading());

        turnRight(targetAngle);
        ahead(Math.hypot(dx, dy));
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        super.onScannedRobot(event);

        // Coordinates of the point where the enemy is in relation to the robot
        Point2D.Double enemyPoint = getCoordenadasInimigo(event.getBearing(), event.getDistance());
        enemyPoint.x -= this.getWidth() * 2.5 / 2; // Adjusts the position of the point relative to the robot size
        enemyPoint.y -= this.getHeight() * 2.5 / 2;

        // Create a rectangle around the point where the enemy was detected
        Rectangle enemyRect = new Rectangle((int) enemyPoint.x, (int) enemyPoint.y, (int) (this.getWidth() * 2.5),
                (int) (this.getHeight() * 2.5));

        // Add the rectangle representing the position of the enemy to the obstacles
        // list
        obstacles.add(enemyRect);

        // Print the obstacle rectangles to the console
        // System.out.println("ENEMY RECTANGLES:");
        // obstacles.forEach(x -> System.out.println(x));
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

    // Método para desenhar no campo de batalha
    public void onPaint(Graphics2D g) {
        super.onPaint(g);

        g.setColor(Color.yellow); // Define a cor da linha

        // desenha retângulos representando os obstáculos
        obstacles.stream().forEach(x -> g.drawRect(x.x, x.y, (int) x.getWidth(), (int) x.getHeight()));

        // se houver pontos no caminho, desenha linhas entre eles
        if (path != null) {
            for (int i = 1; i < path.size(); i++) {
                // Desenha uma linha grossa entre os pontos com a cor rosa
                desenhaLinhaGrossa(g, (int) path.get(i - 1).getX(), (int) path.get(i - 1).getY(),
                        (int) path.get(i).getX(),
                        (int) path.get(i).getY(), 2, Color.PINK);
            }
        }
    }

    /**
     * desenha uma linha grossa com uma determinada grossura e cor
     * 
     * @param g        contexto gráfico no qual desenhar a linha
     * @param x1       coordenada x do ponto inicial da linha
     * @param y1       coordenada y do ponto inicial da linha
     * @param x2       coordenada x do ponto final da linha
     * @param y2       coordenada y do ponto final da linha
     * @param grossura grossura da linha a ser desenhada
     * @param c        cor da linha a ser desenhada
     */
    private void desenhaLinhaGrossa(Graphics2D g, int x1, int y1, int x2, int y2, int grossura, Color c) {
        g.setColor(c); // define a cor da linha

        // diferenças nas coordenadas x e y para determinar o tamanho da linha.
        int dX = x2 - x1;
        int dY = y2 - y1;

        // comprimento da linha usando o teorema de Pitágoras
        double tamanhoLinha = Math.sqrt(dX * dX + dY * dY);

        // escala necessária para desenhar a linha com a grossura especificada
        double escala = (double) (grossura) / (2 * tamanhoLinha);

        // deslocamentos para desenhar a linha com a grossura especificada
        double ddx = -escala * (double) dY;
        double ddy = escala * (double) dX;

        // arredonda os deslocamentos para o inteiro mais próximo
        ddx += (ddx > 0) ? 0.5 : -0.5;
        ddy += (ddy > 0) ? 0.5 : -0.5;

        int dx = (int) ddx;
        int dy = (int) ddy;

        // coordenadas dos pontos que definem a largura da linha
        int xPontos[] = new int[4];
        int yPontos[] = new int[4];

        xPontos[0] = x1 + dx;
        yPontos[0] = y1 + dy;
        xPontos[1] = x1 - dx;
        yPontos[1] = y1 - dy;
        xPontos[2] = x2 - dx;
        yPontos[2] = y2 - dy;
        xPontos[3] = x2 + dx;
        yPontos[3] = y2 + dy;

        g.fillPolygon(xPontos, yPontos, 4); // preenche o polígono formado pelos pontos calculados, criando uma linha
                                            // com a grossura especificada
    }
}
