package problemaB;

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
        }

        Chromosome() {
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
    static final int POP_SIZE = 100;

    /**
     * quantidade de melhores indivíduos que serão mantidos inalterados na próxima
     * geração. Eles são selecionados com base em seu fitness.
     */
    static final int TOP = 10;

    /**
     * número máximo de gerações que o algoritmo genético irá executar antes de
     * parar
     */
    static final int MAX_GENERATIONS = 10;

    /**
     * taxa de mutação controla a probabilidade de que um gene em um cromossomo
     * sofra uma mutação durante a reprodução
     */
    static final double MUTATION_RATE = 50;

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

    static final Random rand = new Random();

    public static Point START = new Point(rand.nextDouble() * GeneticAlgorithmBot.conf.getWidth(),
            rand.nextDouble() * GeneticAlgorithmBot.conf.getHeight()); // ponto inicial
    public static Point END = new Point(rand.nextDouble() * GeneticAlgorithmBot.conf.getWidth(),
            rand.nextDouble() * GeneticAlgorithmBot.conf.getHeight()); // ponto final

    public static List<Chromosome> population;
    public static List<Chromosome> newPopulationFix;

    // Método para encontrar o caminho
    public List<Point> findPath() {
        population = initializePopulation();
        int generation = 0;

        while (generation < MAX_GENERATIONS) {

            evaluateFitness(population);

            // System.out.println("POPULATION AFTER FITNESS: " + population);

            newPopulationFix = evolvePopulation(population);

            // System.out.println("newPopulationFix : " + newPopulationFix);

            // population.clear();

            // population = newPopulationFix;

            // System.out.println("Gen " + generation + ": Best fitness = " +
            /// bestFitness);
            generation++;
        }

        System.out.println("TOP " + TOP + ": ");

        // topPChromosomes = populationAscendingOrder(population);
        Collections.sort(newPopulationFix);

        for (int i = 0; i < TOP; i++) {
            System.out.println(newPopulationFix.get(i));
        }

        System.out.println(
                "------------------------------------------------------//------------------------------------------------------");

        return newPopulationFix.get(0).getPath();
    }

    // Inicializa a população com cromossomos aleatórios
    private List<Chromosome> initializePopulation() {

        List<Chromosome> population2 = new ArrayList<>();

        for (int i = 0; i < POP_SIZE; i++) {

            List<Point> path = generateRandomPath();
            population2.add(new Chromosome(path));
        }

        // System.out.println("Initial population: " + population);
        return population2;
    }

    // Gera um caminho aleatório entre START e END
    private List<Point> generateRandomPath() {
        List<Point> path = new ArrayList<>();
        path.add(START);
        int pontosIntermedios = rand.nextInt(5) + 1;

        for (int i = 0; i < pontosIntermedios; i++) { // Exemplo de 5 pontos intermedios
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

        // Inicializa a variável para armazenar a distância total percorrida
        double totalDistance = 0.0;

        // Obtém o caminho do cromossomo
        List<Point> path = chrom.getPath();

        // Itera sobre os pontos do caminho (exceto o primeiro)
        for (int i = 1; i < path.size(); i++) {
            // Obtém o ponto atual e o ponto anterior no caminho
            Point current = path.get(i);
            Point previous = path.get(i - 1);

            // Calcula a distância entre o ponto anterior e o ponto atual
            double segmentDistance = distance(previous, current);

            // Adiciona a distância ao total
            totalDistance += segmentDistance;
        }

        // Retorna a distância total percorrida
        return totalDistance;
    }


    // Calcula a distância entre dois pontos
    private double distance(Point a, Point b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
    }

    // Evolui a população através de seleção, cruzamento e mutação
    private List<Chromosome> evolvePopulation(List<Chromosome> population) {
        List<Chromosome> newPopulation = new ArrayList<Chromosome>();

        int numMutations = POP_SIZE * POP_MUTATION / 100;
        int numHereditary = POP_SIZE * POP_HEREDITARY / 100;
        int numCrossover = POP_SIZE * POP_CROSS / 100;

        // Avaliar fitness da população atual
        // evaluateFitness(population);

        // Ordenar a população com base no fitness (do menor para o maior)
        Collections.sort(population);

        // Adicionar os indivíduos hereditários à nova população
        newPopulation.addAll(population.subList(0, numHereditary));

        // Realizar cruzamento e adicionar os filhos à nova população
        for (int i = 0; i < numCrossover; i++) {
            Chromosome parent1 = selectParent(population);
            Chromosome parent2 = selectParent(population);
            Chromosome child = crossover(parent1, parent2);

            child.setFitness(calculateFitness(child));

            newPopulation.add(child);
        }

        // Selecionar e mutar indivíduos, adicionando-os à nova população
        for (int i = 0; i < numMutations; i++) {
            Chromosome chrom = population.get(rand.nextInt(POP_SIZE));
            mutate(chrom);
            newPopulation.add(chrom);
        }

        // Completar a nova população com indivíduos aleatórios da população atual
        while (newPopulation.size() < POP_SIZE) {
            Chromosome parent = selectParent(population);
            newPopulation.add(parent);
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
        List<Point> path = chrom.getPath();

        // Ignora o primeiro e o último ponto
        for (int i = 1; i < path.size() - 1; i++) {
            Point p = path.get(i);
            if (rand.nextDouble() < (MUTATION_RATE / 100.0)) {
                // Realiza a mutação no ponto
                p.setX(rand.nextDouble() * GeneticAlgorithmBot.conf.getWidth());
                p.setY(rand.nextDouble() * GeneticAlgorithmBot.conf.getHeight());
                mutated = true; // Indica que houve mutação
                // System.out.println( "Mutated point from (" + oldX + ", " + oldY + ") to (" +
                // p.getX() + ", " + p.getY() + ")");
            }
        }

        // Se houve mutação, recalcula o fitness
        if (mutated) {
            chrom.setFitness(calculateFitness(chrom));
            // System.out.println("Chromosome after mutation: " + chrom.fitness);
        }
    }

    /**
     * População ordenada de forma decrescente.
     * 
     * @param population A lista de cromossomos a ser ordenada.
     * @return A lista ordenada de forma decrescente.
     */
    public List<Chromosome> populationDescendingOrder(List<Chromosome> population) {
        // Cópia da lista de população para não modificar a original
        List<Chromosome> sortedPopulation = new ArrayList<>(population);

        // Bubble Sort para ordenar de forma decrescente
        for (int i = 0; i < sortedPopulation.size() - 1; i++) {
            for (int j = 0; j < sortedPopulation.size() - i - 1; j++) {
                // Compara o valor de fitness de dois cromossomos
                if (sortedPopulation.get(j).getFitness() < sortedPopulation.get(j + 1).getFitness()) {
                    // Troca os cromossomos de posição se estiverem fora de ordem
                    Chromosome temp = sortedPopulation.get(j);
                    sortedPopulation.set(j, sortedPopulation.get(j + 1));
                    sortedPopulation.set(j + 1, temp);
                }
            }
        }

        return sortedPopulation;
    }

    /**
     * População ordenada de forma crescente.
     * 
     * @param population A lista de cromossomos a ser ordenada.
     * @return A lista ordenada de forma crescente.
     */
    /**
     * População ordenada de forma crescente.
     * 
     * @param population A lista de cromossomos a ser ordenada.
     * @return A lista ordenada de forma crescente.
     */
    public List<Chromosome> populationAscendingOrder(List<Chromosome> population) {
        // Cópia da lista de população para não modificar a original
        List<Chromosome> sortedPopulation = new ArrayList<>(population);

        // Bubble Sort para ordenar de forma crescente
        for (int i = 0; i < sortedPopulation.size() - 1; i++) {
            for (int j = 0; j < sortedPopulation.size() - i - 1; j++) {
                // Compara o valor de fitness de dois cromossomos
                if (sortedPopulation.get(j).getFitness() > sortedPopulation.get(j + 1).getFitness()) {
                    // Troca os cromossomos de posição se estiverem fora de ordem
                    Chromosome temp = sortedPopulation.get(j);
                    sortedPopulation.set(j, sortedPopulation.get(j + 1));
                    sortedPopulation.set(j + 1, temp);
                }
            }
        }

        return sortedPopulation;
    }
}