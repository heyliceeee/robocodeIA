package sampleRobots;

import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.DeathEvent;
import robocode.HitWallEvent;
import robocode.RobocodeFileOutputStream;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;
import utils.Utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import impl.Point;
import impl.UIConfiguration;
import interf.IPoint;
import problemaB.AlgoritmoGenetico;
import problemaB.Populacao;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * SirkKazzio - a class by Alice Dias
 */
public class SirKazzio extends AdvancedRobot {
    // #region VARIÁVEIS

    // #region CONFIGURAÇÕES DO ALGORITMO GENERICO
    /**
     * taxa de mutação controla a probabilidade de que um gene em um cromossomo
     * sofra uma mutação durante a reprodução
     */
    public static final int MUTATION_RATE = 2;

    /**
     * tamanho da população
     */
    public static final int POP_SIZE = 100;

    /**
     * percentagem da população que será selecionada para a reprodução com base no
     * seu fitness
     */
    public static final int POP_HEREDITARY = 50;

    /**
     * percentagem de população que sofrerá mutação após a reprodução
     */
    public static final int POP_MUTATION = 20;

    /**
     * percentagem da população que será criada por cruzamento entre indivíduos
     * selecionados para reprodução
     */
    public static final int POP_CROSS = 30;

    /**
     * número máximo de gerações que o algoritmo genético irá executar antes de
     * parar
     */
    public static final int MAX_ITERATIONS = 100;

    /**
     * percentagem de melhores indivíduos que serão mantidos inalterados na próxima
     * geração. Eles são selecionados com base em seu fitness.
     */
    public static final int TOP = 50;

    // #endregion

    /**
     * lista de obstaculos preenchida ao fazer scan
     */
    public static List<Rectangle> obstaculos;

    /**
     * configurações
     */
    public static UIConfiguration conf;

    /**
     * lista de pontos do mapa
     */
    public static List<IPoint> pontos;

    /**
     * associar inimigos a retângulos e permitir remover retângulos de inimigos já
     * desatualizados
     */
    public static HashMap<String, Rectangle> inimigos;

    /**
     * contém o ponto atual para o qual o robot se está a dirigir
     */
    public static int pontoAtual = -1;

    // #endregion

    @Override
    public void run() {
        super.run();

        customizarRobo();

        // #region inicializar

        obstaculos = new ArrayList<>();
        inimigos = new HashMap<>();
        conf = new UIConfiguration((int) getBattleFieldWidth(), (int) getBattleFieldHeight(), obstaculos); // tamanho

        // #endregion

        System.out.println("Iniciando execução...");

        while (true) {
            this.setTurnRadarRight(360); // continuamente verifica quais sao os obstaculos perto de si

            if (pontoAtual < 0) {
                gerarNovoCaminho();
            }

            // se está a dirigir para algum ponto
            if (pontoAtual >= 0) {
                IPoint ponto = pontos.get(pontoAtual); // coordenadas do ponto atual

                // se já está no ponto ou lá perto...
                if (Utils.getDistance(this, ponto.getX(), ponto.getY()) < 2) {
                    pontoAtual++;

                    // se chegou ao fim do caminho
                    if (pontoAtual >= pontos.size()) {
                        pontoAtual = -1;
                    }
                }

                RoboVaiPara(this, ponto.getX(), ponto.getY());
            }

            this.execute();
        }
    }

    /**
     * a batalha terminou
     */
    @Override
    public void onBattleEnded(BattleEndedEvent event) {

    }

    /**
     * o robo morreu
     */
    @Override
    public void onDeath(DeathEvent event) {
        super.onDeath(event);
    }

    /**
     * o robo ganhou
     */
    @Override
    public void onWin(WinEvent event) {
        super.onWin(event); // Chama o método onDeath da superclasse

    }

    /**
     * colidiu com a parede
     */
    @Override
    public void onHitWall(HitWallEvent event) {
        super.onHitWall(event);

    }

    // #region B) MOVIMENTACAO

    /**
     * atraves da geracao de um ponto random, vai gerar um caminho até a esse ponto,
     * de forma a desviar dos obstaculos e no menor trajeto possivel
     */
    private void gerarNovoCaminho() {
        Random rand = new Random();

        conf.setStart(new Point((int) this.getX(), (int) this.getY())); // ponto Inicial

        int pontoFinal_x = rand.nextInt(conf.getWidth());
        int pontoFinal_y = rand.nextInt(conf.getHeight());

        conf.setEnd(new Point(pontoFinal_x, pontoFinal_y)); // ponto Final

        System.out.println("Gerando novo caminho...");
        System.out.println("Destino: (" + pontoFinal_x + ", " + pontoFinal_y + ")");

        Populacao populacao = new Populacao(SirKazzio.POP_SIZE, conf);

        for (int i = 0; i < SirKazzio.MAX_ITERATIONS; i++) {
            populacao = AlgoritmoGenetico.evoluirPopulacao(populacao, conf);
            System.out.println("Geração " + (i + 1) + ": best fitness = " + populacao.getMelhor().fitnessFunction);
        }

        pontos = populacao.getMelhor().caminho;
        pontoAtual = 0;

        System.out.println("Caminho gerado: " + pontos);
    }

    /**
     * robo vai para determinadas coordenadas
     * 
     * @param robo
     * @param x
     * @param y
     */
    private void RoboVaiPara(AdvancedRobot robo, double x, double y) {

        // diferença entre a posição atual do robo e as coordenadas de destino
        x -= robo.getX();
        y -= robo.getY();

        // ângulo para o alvo em relação à posição atual do robo
        double anguloParaAlvo = Math.atan2(x, y);

        // ângulo alvo em relação à direção atual do robo
        double anguloAlvo = robocode.util.Utils.normalRelativeAngle(anguloParaAlvo - Math.toRadians(robo.getHeading()));

        // distância até o destino
        double distancia = Math.hypot(x, y);

        // ângulo de virada necessário para atingir o ângulo alvo
        double anguloVirada = Math.atan(Math.tan(anguloAlvo));

        // define a direção de virada do robo para o ângulo de virada calculado
        robo.setTurnRight(Math.toDegrees(anguloVirada));

        // se o ângulo alvo for igual ao ângulo de virada, avança na distância calculada
        if (anguloAlvo == anguloVirada) {
            robo.setAhead(distancia);
        } else {
            robo.setBack(distancia);
        }

        robo.execute();
    }

    // #endregion

    // #region OUTROS METODOS (NAO MEXER)

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
        obstaculos.remove(rect);

        // Remove o robô inimigo do mapa de inimigos
        inimigos.remove(event.getName());
    }

    /**
     * Escaneia um inimigo e adiciona um retângulo representando sua posição ao
     * redor do robô.
     * 
     * @param event O evento de escaneamento do robô inimigo.
     */
    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        super.onScannedRobot(event); // Chama o método onScannedRobot da superclasse

        // System.out.println("INIMIGO ENCONTRADO: " + event.getName());

        // coordenadas do ponto onde o inimigo está em relação ao robô
        Point2D.Double ponto = getCoordenadasInimigo(this, event.getBearing(), event.getDistance());
        ponto.x -= this.getWidth() * 2.5 / 2; // ajusta a posição do ponto em relação ao tamanho do robô
        ponto.y -= this.getHeight() * 2.5 / 2;

        // cria um retângulo ao redor do ponto onde o inimigo foi detectado
        Rectangle rect = new Rectangle((int) ponto.x, (int) ponto.y, (int) (this.getWidth() * 2.5),
                (int) (this.getHeight() * 2.5));

        // se já existe um retângulo para este inimigo, remove-o da lista de obstáculos
        if (inimigos.containsKey(event.getName())) {
            obstaculos.remove(inimigos.get(event.getName()));
        }

        obstaculos.add(rect); // adiciona o retângulo representando a posição do inimigo à lista de obstáculos
        inimigos.put(event.getName(), rect); // armazena o retângulo associado ao inimigo pelo nome do inimigo

        // impressão dos retângulos de obstáculos no console
        // System.out.println("INIMIGOS EM:");
        // obstaculos.forEach(x -> System.out.println(x));
    }

    /**
     * Calcula as coordenadas de um alvo com base no ângulo e na distância
     * fornecidos.
     * 
     * @param robo      O robô que está realizando o cálculo.
     * @param bearing   O ângulo para o alvo, em graus.
     * @param distancia A distância ao alvo.
     * @return As coordenadas do alvo.
     */
    private Double getCoordenadasInimigo(robocode.Robot robo, double bearing, double distancia) {
        // Converte o ângulo do robô e do alvo para radianos
        double angulo = Math.toRadians((robo.getHeading() + bearing) % 360);

        // coordenadas do alvo com base no ângulo e na distância
        double x = robo.getX() + Math.sin(angulo) * distancia;
        double y = robo.getY() + Math.cos(angulo) * distancia;

        // Retorna as coordenadas do alvo
        return new Point2D.Double(x, y);
    }

    /**
     * mostra o caminho no mapa desenhando linhas entre os pontos.
     * 
     * @param g O contexto gráfico no qual desenhar o caminho.
     */
    @Override
    public void onPaint(Graphics2D g) {
        super.onPaint(g); // chama o método onPaint da superclasse

        g.setColor(Color.YELLOW); // define a cor amarela para os obstáculos

        // desenha retângulos representando os obstáculos
        obstaculos.stream().forEach(x -> g.drawRect(x.x, x.y, (int) x.getWidth(), (int) x.getHeight()));

        // se houver pontos no caminho, desenha linhas entre eles
        if (pontos != null) {
            for (int i = 1; i < pontos.size(); i++) {
                // Desenha uma linha grossa entre os pontos com a cor rosa
                desenhaLinhaGrossa(g, pontos.get(i - 1).getX(), pontos.get(i - 1).getY(), pontos.get(i).getX(),
                        pontos.get(i).getY(), 2, Color.PINK);
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

    /**
     * customizar o robo
     */
    public void customizarRobo() {
        // setColors(body, gun, radar, bullet, scanArc)
        setColors(Color.red, Color.black, Color.lightGray, Color.white, Color.red);
    }

    // #endregion
}