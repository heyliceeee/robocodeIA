package problemaB;

import java.awt.Rectangle;
import java.util.*;

import sampleRobots.GeneticAlgorithmBot;

public class GeneticAlgorithm {
    // Classe para representar um ponto
    public static class Point {
        double x, y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    // Classe para representar um cromossomo
    public static class Chromosome implements Comparable<Chromosome> {
        List<Point> path;
        double fitness;

        Chromosome(List<Point> path) {
            this.path = new ArrayList<>(path);
            this.fitness = 0.0;
        }

        public List<Point> getPath() {
            return path;
        }

        public void setPath(List<Point> path) {
            this.path = path;
        }

        public double getFitness() {
            return fitness;
        }

        public void setFitness(double fitness) {
            this.fitness = fitness;
        }

        @Override
        public String toString() {
            return "Chromosome [path=" + path + ", fitness=" + fitness + "]";
        }

        @Override
        public int compareTo(Chromosome o) {
            return Double.compare(this.getFitness(), o.getFitness());
        }
    }

    // Definições gerais

    /**
     * tamanho da população
     */
    static final int POP_SIZE = 1000;

    /**
     * quantidade de melhores indivíduos que serão mantidos inalterados na próxima
     * geração. Eles são selecionados com base em seu fitness.
     */
    static final int TOP = 10;

    /**
     * número máximo de gerações que o algoritmo genético irá executar antes de
     * parar
     */
    static final int MAX_GENERATIONS = 1000;

    /**
     * taxa de mutação controla a probabilidade de que um gene em um cromossomo
     * sofra uma mutação durante a reprodução
     */
    static final double MUTATION_RATE = 0.05;

    /**
     * percentagem da população que será selecionada para a reprodução com base no
     * seu fitness
     */
    public static final int POP_HEREDITARY = 50;

    /**
     * percentagem de população que sofrerá mutação após a reprodução
     */
    public static final int POP_MUTATION = 2;

    /**
     * percentagem da população que será criada por cruzamento entre indivíduos
     * selecionados para reprodução
     */
    public static final int POP_CROSS = 3;

    static final Point START = new Point(0.0, 0.0); // ponto inicial
    static final Random rand = new Random();
    static final Point END = new Point(rand.nextDouble() * GeneticAlgorithmBot.conf.getWidth(),
            rand.nextDouble() * GeneticAlgorithmBot.conf.getHeight()); // ponto final
    static List<Point> obstacles = new ArrayList<>(); // Exemplo de obstáculo

    // Método para encontrar o caminho
    public List<Point> findPath() {
        List<Chromosome> population = initializePopulation();
        int generation = 0;
        Chromosome bestChromosome = null;
        double bestFitness = Double.NEGATIVE_INFINITY;
        List<Chromosome> topPChromosomes = new ArrayList<Chromosome>();

        while (generation < MAX_GENERATIONS) {

            evaluateFitness(population);

            population = evolvePopulation(population);

            Chromosome best = getBestChromosome(population);

            double currentBestFitness = best.getFitness();

            if (currentBestFitness > bestFitness) {

                bestFitness = currentBestFitness;
                bestChromosome = best;
            }

            // System.out.println("Gen " + generation + ": Best fitness = " +
            /// bestFitness);
            generation++;
        }

        topPChromosomes = getTopChromosomes(population);
        System.out.println("TOP " + TOP + ": ");

        for (int i = 0; i < topPChromosomes.size(); i++) {
            System.out.println(topPChromosomes.get(i));
        }

        System.out.println(
                "------------------------------------------------------//------------------------------------------------------");

        // Verifica se algum cromossomo foi encontrado
        if (bestChromosome != null) {

            System.out.println("Best path found: " + bestChromosome.getPath());

            return bestChromosome.getPath();

        } else {
            System.out.println("No path found.");

            return new ArrayList<>(); // Retorna uma lista vazia se nenhum cromossomo foi encontrado
        }
    }

    // Inicializa a população com cromossomos aleatórios
    private List<Chromosome> initializePopulation() {

        List<Chromosome> population = new ArrayList<>();

        for (int i = 0; i < POP_SIZE; i++) {

            List<Point> path = generateRandomPath();
            population.add(new Chromosome(path));
        }

        // System.out.println("Initial population: " + population);
        return population;
    }

    // Gera um caminho aleatório entre START e END
    private List<Point> generateRandomPath() {
        List<Point> path = new ArrayList<>();
        path.add(START);
        int pontosIntermedios = rand.nextInt(6);

        for (int i = 1; i < pontosIntermedios; i++) { // Exemplo de 5 pontos intermedios
            double x = 0.0, y = 0.0;

            // pontos intermedios nao podem ser iguais aos pontos inicial e final
            do {
                x = rand.nextDouble() * GeneticAlgorithmBot.conf.getWidth();
                y = rand.nextDouble() * GeneticAlgorithmBot.conf.getHeight();

            } while ((x == START.getX() && y == START.getY()) || (x == END.getX() && y == END.getY()));

            path.add(new Point(x, y));
        }
        path.add(END);
        return path;
    }

    // Avalia o fitness de cada cromossomo na população
    private void evaluateFitness(List<Chromosome> population) {
        // int i = 0;

        for (Chromosome chrom : population) {
            chrom.setFitness(calculateFitness(chrom));
        }
    }

    // Calcula o fitness de um cromossomo
    private double calculateFitness(Chromosome chrom) {
        // Componente 1: Número de pontos intermediários (quanto menor, melhor)
        int numIntermediatePoints = chrom.getPath().size() - 2; // Exclui os pontos START e END
        double intermediatePointsScore = 1.0 / (1 + numIntermediatePoints);

        // Componente 2: Distância do ponto inicial ao ponto final (quanto menor,
        // melhor)
        double totalDistance = 0.0;
        Point startPoint = chrom.getPath().get(0);
        Point endPoint = chrom.getPath().get(chrom.getPath().size() - 1);
        totalDistance += distance(startPoint, endPoint);

        // Combina os componentes com os pesos
        double fitness = 0.2 * intermediatePointsScore + 0.8 * (1.0 / (1 + totalDistance));

        // Inverte o valor para que um valor mais alto de fitness represente uma melhor
        // solução
        return fitness;
    }

    // Calcula a distância entre dois pontos
    private double distance(Point a, Point b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    // Evolui a população através de seleção, cruzamento e mutação
    private List<Chromosome> evolvePopulation(List<Chromosome> population) {
        List<Chromosome> newPopulation = new ArrayList<>();

        // Seleção e cruzamento
        while (newPopulation.size() < POP_SIZE) {
            Chromosome parent1 = selectParent(population);
            Chromosome parent2 = selectParent(population);
            Chromosome child = crossover(parent1, parent2);
            newPopulation.add(child);
        }

        // Mutação
        for (Chromosome chrom : newPopulation) {
            mutate(chrom);
        }

        // System.out.println("New population: " + newPopulation);
        return newPopulation;
    }

    // Seleciona um cromossomo da população (seleção por torneio)
    private Chromosome selectParent(List<Chromosome> population) {
        Chromosome best = population.get(rand.nextInt(POP_SIZE));

        for (int i = 0; i < 3; i++) { // Torneio com 3 competidores

            Chromosome competitor = population.get(rand.nextInt(POP_SIZE));

            if (competitor.getFitness() > best.getFitness()) {
                best = competitor;
            }
        }
        return best;
    }

    // Realiza o cruzamento de um ponto entre dois cromossomos
    private Chromosome crossover(Chromosome parent1, Chromosome parent2) {
        int crossoverPoint = rand.nextInt(Math.min(parent1.getPath().size(), parent2.getPath().size()));
        List<Point> newPath = new ArrayList<>();

        newPath.addAll(parent1.getPath().subList(0, crossoverPoint));
        newPath.addAll(parent2.getPath().subList(crossoverPoint, parent2.getPath().size()));

        return new Chromosome(newPath);
    }

    // Realiza a mutação em um cromossomo
    private void mutate(Chromosome chrom) {
        boolean mutated = false; // Flag para indicar se houve mutação

        for (Point p : chrom.getPath()) {
            if (rand.nextDouble() < MUTATION_RATE) {
                // Realiza a mutação no ponto
                p.setX(rand.nextDouble() * GeneticAlgorithmBot.conf.getWidth()); // Ajuste conforme necessário
                p.setY(rand.nextDouble() * GeneticAlgorithmBot.conf.getHeight()); // Ajuste conforme necessário
                mutated = true; // Indica que houve mutação
            }
        }

        // Se houve mutação, recalcula o fitness
        if (mutated) {
            chrom.setFitness(calculateFitness(chrom));
        }
    }

    // Obtém o melhor cromossomo da população
    public Chromosome getBestChromosome(List<Chromosome> population) {

        // Ordena a população com base no valor de fitness (do maior para o menor)
        Collections.sort(population, Collections.reverseOrder());

        // Retorna o primeiro cromossomo da lista, que será o que possui o maior fitness
        return population.get(0);
    }

    // Obtém os melhores top x cromossomos da população
    public List<Chromosome> getTopChromosomes(List<Chromosome> population) {

        List<Chromosome> top = new ArrayList<Chromosome>();

        // Ordena a população com base no valor de fitness (do maior para o menor)
        Collections.sort(population, Collections.reverseOrder());

        for (int i = 0; i < TOP; i++) {
            top.add(population.get(i));
        }

        // Retorna a lista, que será os que possui o maior fitness
        return top;
    }
}