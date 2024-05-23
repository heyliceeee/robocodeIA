package sampleRobots;

import robocode.*;
import robocode.util.Utils;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.IOException;
import java.awt.*;
import java.util.*;
import java.util.List;

import impl.UIConfiguration;
import problemaB.GeneticAlgorithm;
import problemaB.GeneticAlgorithm.Chromosome;

public class GeneticAlgorithmBot extends AdvancedRobot {

    // #region VARIAVEIS

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

    static final Random rand = new Random();

    // #endregion

    public void run() {
        // #region Configurações iniciais do robô
        setColors(Color.red, Color.blue, Color.green); // Corpo, canhão e radar
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        // #endregion

        // #region inicializar

        obstacles = new ArrayList<>();
        inimigos = new HashMap<>();
        conf = new UIConfiguration((int) getBattleFieldWidth(), (int) getBattleFieldHeight(), obstacles); // tamanho

        // #endregion

        // Gera o caminho usando o algoritmo genético
        GeneticAlgorithm ga = new GeneticAlgorithm();
        path = ga.findPath();

        // Movimenta-se ao longo do caminho
        while (true) {
            this.setTurnRadarRight(360); // continuamente verifica quais sao os obstaculos perto de si
            moveAlongPath();
            execute(); // Executa as ações pendentes (movimento, rotação, etc.)
        }
    }

    @Override
    public void onBattleEnded(BattleEndedEvent event) {

        exportarMovimentacaoGraficoFicheiro();
        exportarTopMovimentosFicheiro();
    }

    /**
     * exportar o caminho realizados na partida, para um .txt
     */
    private void exportarMovimentacaoGraficoFicheiro() {
        try {

            RobocodeFileOutputStream outputStream = new RobocodeFileOutputStream(getDataFile("movimentacaoGraph.txt"));

            // Collections.sort(GeneticAlgorithm.newPopulationFix);
            int i = 0;

            List<Chromosome> firstPop = new ArrayList<Chromosome>(GeneticAlgorithm.MAX_GENERATIONS);

            firstPop = GeneticAlgorithm.bestFitnessPop;

            for (Chromosome solucao : firstPop) {

                outputStream.write((solucao.getFitness() +
                        System.lineSeparator()).getBytes());

                        i++;

                if (i >= GeneticAlgorithm.MAX_GENERATIONS) {
                    break;
                }
            }
            outputStream.close();
            System.out.println("Lista exportada com sucesso para o arquivo: movimentacaoGraph.txt");

        } catch (IOException e) {
            System.err.println("Erro ao exportar lista para arquivo: " + e.getMessage());
        }
    }

    /**
     * exportar os top movimentos realizados na partida, para um .txt
     */
    private void exportarTopMovimentosFicheiro() {
        try {

            RobocodeFileOutputStream outputStream = new RobocodeFileOutputStream(getDataFile("movimentacaoTop.txt"));

            int i = 0;

            List<Chromosome> firstPop = new ArrayList<Chromosome>(GeneticAlgorithm.POP_SIZE);

            firstPop = GeneticAlgorithm.bestFitnessPop;

            Collections.sort(firstPop);

            for (Chromosome solucao : firstPop) {

                outputStream.write((solucao +
                        System.lineSeparator()).getBytes());

                i++;

                if (i >= GeneticAlgorithm.TOP) {
                    break;
                }
            }

            outputStream.close();
            System.out.println("Lista exportada com sucesso para o arquivo: movimentacaoTop.txt");

        } catch (IOException e) {
            System.err.println("Erro ao exportar lista para arquivo: " + e.getMessage());
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

            GeneticAlgorithm.START = new GeneticAlgorithm.Point(this.getY(), this.getX()); // ponto inicial
            GeneticAlgorithm.START = new GeneticAlgorithm.Point(rand.nextDouble() * GeneticAlgorithmBot.conf.getWidth(),
                    rand.nextDouble() * GeneticAlgorithmBot.conf.getHeight()); // ponto final

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

    /**
     * quando um robô inimigo é destruído.
     * 
     * @param event O evento de morte do robô.
     */
    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        super.onRobotDeath(event); // Chama o método onRobotDeath da superclasse

        // Obtém o retângulo associado ao robô inimigo que foi morto
        Rectangle rect = inimigos.get(event.getName());

        // Remove o retângulo da lista de obstáculos
        obstacles.remove(rect);

        // Remove o robô inimigo do mapa de inimigos
        inimigos.remove(event.getName());
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

        // se já existe um retângulo para este inimigo, remove-o da lista de obstáculos
        if (inimigos.containsKey(event.getName())) {
            obstacles.remove(inimigos.get(event.getName()));
        }

        obstacles.add(enemyRect); // adiciona o retângulo representando a posição do inimigo à lista de obstáculos
        inimigos.put(event.getName(), enemyRect); // armazena o retângulo associado ao inimigo pelo nome do inimigo
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
        for (Rectangle obstacle : obstacles) {
            g.drawRect(obstacle.x, obstacle.y, (int) obstacle.getWidth(), (int) obstacle.getHeight());
        }
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
