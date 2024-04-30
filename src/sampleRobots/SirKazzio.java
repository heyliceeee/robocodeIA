package sampleRobots;

import robocode.AdvancedRobot;
import robocode.DeathEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import utils.Utils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import algoritmoGenetico.Solution;
import impl.Point;
import impl.UIConfiguration;
import interf.IPoint;

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
     * número de melhores indivíduos que serão mantidos inalterados na próxima
     * geração. Eles são selecionados com base em seu fitness.
     */
    public static final int TOP = 10;

    // #endregion

    /**
     * lista de obstaculos preenchida ao fazer scan
     */
    private List<Rectangle> obstaculos;

    /**
     * configurações
     */
    private UIConfiguration conf;

    /**
     * lista de pontos do mapa
     */
    private ArrayList<IPoint> pontos;

    /**
     * associar inimigos a retângulos e permitir remover retângulos de inimigos já
     * desatualizados
     */
    private HashMap<String, Rectangle> inimigos;

    /**
     * ponto atual para o qual o robo está a se dirigir
     */
    private int pontoAtual = -1;

    Random rand = new Random();

    /**
     * soluções da primeira geracao
     */
    private ArrayList<Solution> ger0;

    /**
     * soluções da nova geracao
     */
    private ArrayList<Solution> novaGer;

    // #endregion

    @Override
    public void run() {
        super.run();

        customizarRobo();

        // #region inicializar
        obstaculos = new ArrayList<>();
        inimigos = new HashMap<>();
        conf = new UIConfiguration((int) getBattleFieldWidth(), (int) getBattleFieldHeight(), obstaculos); // tamanho
                                                                                                           // mapa

        // TODO: ARRANJAR! TEM DE ALGUMA FORMA GUARDAR TODOS OS PONTOS E O TOTAL PONTOS
        // INTERMEDIOS
        ger0 = inicializarGeracao0(); // solucao
        // #endregion

        while (true) {
            for (int i = 1; i <= MAX_ITERATIONS; i++) {

                // se existir dados no ger0
                if (!ger0.isEmpty() && ger0.size() <= POP_SIZE) {
                    Collections.sort(ger0, Collections.reverseOrder()); // ordenar individuos com o fitness maior

                    try {
                        System.out.println("GEN: " + i + ", Best Fitness: " +
                                ger0.get(0).getFitnessFunction() + "\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Lista ger0 não possui elementos suficientes.");
                }

                // Seleção + Reprodução
                // Estratégia: manter os top POPHETERARY soluções, gerar POPMUTATION
                // por mutação e POPCROSS por cruzamento
                novaGer = new ArrayList<>();

                // Manter o top POPHETERARY
                for (int j = 0; j < POP_HEREDITARY; j++) {
                    novaGer.add(ger0.get(j)); // adicionar à nova geração
                }

                // Mutação das top POPMUTATION
                for (int j = 0; j < POP_MUTATION; j++) {
                    Solution copia = new Solution(ger0.get(j)); // deep copy

                    copia.mutate(); // mutacao da cópia
                    novaGer.add(copia); // adicionar à nova geração
                }

                // Gerar POPCROSS por cruzamento com base nas top POPCROSS Mutação é
                // feita entre cada duas soluções consecutivas, poderiam ser escolhidas
                // random...
                for (int j = 0; j < POP_CROSS; j += 2) {
                    Solution pai = new Solution(ger0.get(j)); // deep copy
                    Solution mae = new Solution(ger0.get(j + 1)); // deep copy

                    Solution[] filhos = pai.cross(mae); // cruzamento

                    novaGer.add(filhos[0]);
                    novaGer.add(filhos[1]);
                }

                // atualizar geração para a próxima iteração
                ger0 = novaGer;

                this.setTurnRadarRight(360);

                // Se não há um caminho atual ou o robô chegou ao fim do caminho atual
                if (pontoAtual == -1 || pontoAtual >= pontos.size()) {
                    // Gera um novo caminho aleatório
                    gerarCaminhoRandom();
                }

                // Se ainda há pontos no caminho, move-se em direção ao próximo ponto
                if (pontoAtual >= 0 && pontoAtual < pontos.size()) {
                    IPoint ponto = pontos.get(pontoAtual);
                    // Se já está no ponto ou lá perto...
                    if (Utils.getDistance(this, ponto.getX(), ponto.getY()) < 2) {
                        pontoAtual++;
                    }
                    // Move-se em direção ao próximo ponto no caminho
                    RoboVaiPara(this, ponto.getX(), ponto.getY());
                }

                this.execute();
            }

            System.out.println("LISTA DE PONTOS DO ROBO " + pontos);

            definirFuncaoFitness();

            Collections.sort(ger0, Collections.reverseOrder());

            // apos de percorrer as n geracoes, mostra os top getTop()
            for (int i = 0; i < TOP; i++) {
                System.out.println(ger0.get(i));
            }
        }
    }

    /**
     * definir a funcao fitness
     * 
     * @return
     */

    public void definirFuncaoFitness() {

        for (int i = 0; i < POP_SIZE; i++) {
            ger0.add(new Solution(pontos));
        }
    }

    /**
     * criar n genes, com caminho
     * 
     * @return
     */

    public ArrayList<Solution> inicializarGeracao0() {
        ArrayList<Solution> gen0 = new ArrayList<Solution>(POP_SIZE);

        for (int i = 0; i < POP_SIZE; i++) {
            gen0.add(new Solution());
        }

        return gen0;
    }

    /**
     * o robo morreu
     */
    @Override
    public void onDeath(DeathEvent event) {
        super.onDeath(event); // Chama o método onDeath da superclasse

    }

    // #region MOVIMENTAR O ROBO

    /**
     * Gera um novo caminho aleatório.
     */
    private void gerarCaminhoRandom() {
        conf.setStart(new Point((int) this.getX(), (int) this.getY()));
        // Define o ponto de destino como uma posição aleatória no campo de batalha
        conf.setEnd(new Point(rand.nextInt(conf.getWidth()), rand.nextInt(conf.getHeight())));

        // Gera um novo caminho aleatório com um número aleatório de nós intermediários
        pontos = new ArrayList<>();
        pontos.add(new Point((int) this.getX(), (int) this.getY())); // Adiciona o ponto de partida
        int size = rand.nextInt(5); // Cria um caminho aleatório com no máximo 5 nós intermédios (excetuando início
                                    // e fim)
        for (int i = 0; i < size; i++) {
            pontos.add(new Point(rand.nextInt(conf.getWidth()), rand.nextInt(conf.getHeight()))); // Adiciona nós
                                                                                                  // intermediários
                                                                                                  // aleatórios
        }
        pontos.add(conf.getEnd()); // Adiciona o ponto de destino
        pontoAtual = 0; // Define o ponto atual como o ponto de partida
    }

    /**
     * colidiu com a parede
     */
    @Override
    public void onHitWall(HitWallEvent event) {
        super.onHitWall(event);

        // Define o ponto de destino como uma posição aleatória no campo de batalha,
        // evitando a parede
        int newX = rand.nextInt(conf.getWidth());
        int newY = rand.nextInt(conf.getHeight());

        while (!isSafe(newX, newY)) {
            newX = rand.nextInt(conf.getWidth());
            newY = rand.nextInt(conf.getHeight());
        }

        conf.setEnd(new Point(newX, newY));

        // Gera um novo caminho aleatório
        gerarCaminhoRandom();
    }

    /**
     * Verifica se uma posição é segura (não está dentro de uma parede).
     * 
     * @param x A coordenada x da posição a ser verificada.
     * @param y A coordenada y da posição a ser verificada.
     * @return true se a posição é segura, false caso contrário.
     */
    private boolean isSafe(int x, int y) {
        for (Rectangle obstaculo : obstaculos) {
            if (obstaculo.contains(x, y)) {
                return false;
            }
        }

        return true;
    }

    /**
     * robo vai para determinadas coordenadas
     * 
     * @param robo
     * @param x
     * @param y
     */
    private void RoboVaiPara(AdvancedRobot robo, int x, int y) {
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

        System.out.println("INIMIGO ENCONTRADO: " + event.getName());

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
        // obstacles.forEach(x -> System.out.println(x));
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
}