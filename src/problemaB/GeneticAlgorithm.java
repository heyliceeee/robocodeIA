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
    static class Chromosome {
        List<Point> path;
        double fitness;

        Chromosome(List<Point> path) {
            this.path = new ArrayList<>(path);
            this.fitness = 0.0;
        }

        @Override
        public String toString() {
            return "Chromosome [path=" + path + ", fitness=" + fitness + "]";
        }
    }

    // Definições gerais
    static final int POPULATION_SIZE = 100;
    static final int MAX_GENERATIONS = 100;
    static final double MUTATION_RATE = 0.05;
    static final Point START = new Point(0, 0); // ponto inicial
    static final Point END = new Point(GeneticAlgorithmBot.conf.getWidth(), GeneticAlgorithmBot.conf.getHeight()); // ponto
                                                                                                                   // final
    static List<Point> obstacles = new ArrayList<>(); // Exemplo de obstáculo

    // Método para encontrar o caminho
    public List<Point> findPath() {
        List<Chromosome> population = initializePopulation();
        int generation = 0;

        while (generation < MAX_GENERATIONS) {
            evaluateFitness(population);
            population = evolvePopulation(population);
            Chromosome best = getBestChromosome(population);

            System.out.println("Generation " + generation + ": Best fitness = " + best.fitness);
            generation++;
        }

        Chromosome best = getBestChromosome(population);
        System.out.println("Best path found: " + best.path);
        return best.path;
    }

    // Inicializa a população com cromossomos aleatórios
    private List<Chromosome> initializePopulation() {
        List<Chromosome> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            List<Point> path = generateRandomPath();
            population.add(new Chromosome(path));
        }

        System.out.println("Initial population: " + population);
        return population;
    }

    // Gera um caminho aleatório entre START e END
    private List<Point> generateRandomPath() {
        List<Point> path = new ArrayList<>();
        path.add(START);
        Random rand = new Random();
        for (int i = 1; i < 5; i++) { // Exemplo de 5 pontos intermediários
            path.add(new Point(rand.nextDouble() * GeneticAlgorithmBot.conf.getWidth(),
                    rand.nextDouble() * GeneticAlgorithmBot.conf.getHeight()));
        }
        path.add(END);
        return path;
    }

    // Avalia o fitness de cada cromossomo na população
    private void evaluateFitness(List<Chromosome> population) {
        for (Chromosome chrom : population) {
            chrom.fitness = calculateFitness(chrom);
        }

        System.out.println("Fitness evaluated: " + population);
    }

    // Calcula o fitness de um cromossomo
    private double calculateFitness(Chromosome chrom) {
        double totalDistance = 0.0;
        for (int i = 0; i < chrom.path.size() - 1; i++) {
            totalDistance += distance(chrom.path.get(i), chrom.path.get(i + 1));
        }
        double collisionPenalty = checkCollisions(chrom.path) ? 1000.0 : 0.0;
        return 1 / (totalDistance + collisionPenalty);
    }

    // Calcula a distância entre dois pontos
    private double distance(Point a, Point b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    // Verifica se o caminho colide com algum obstáculo
    private boolean checkCollisions(List<Point> path) {
        for (Point p : path) {
            for (Point obstacle : obstacles) {
                if (Math.abs(p.x - obstacle.x) < 20 && Math.abs(p.y - obstacle.y) < 20) { // Ajuste conforme necessário
                    return true;
                }
            }
        }
        return false;
    }

    // Evolui a população através de seleção, cruzamento e mutação
    private List<Chromosome> evolvePopulation(List<Chromosome> population) {
        List<Chromosome> newPopulation = new ArrayList<>();
        Random rand = new Random();

        // Seleção e cruzamento
        while (newPopulation.size() < POPULATION_SIZE) {
            Chromosome parent1 = selectParent(population);
            Chromosome parent2 = selectParent(population);
            Chromosome child = crossover(parent1, parent2);
            newPopulation.add(child);
        }

        // Mutação
        for (Chromosome chrom : newPopulation) {
            mutate(chrom);
        }

        System.out.println("New population: " + newPopulation);
        return newPopulation;
    }

    // Seleciona um cromossomo da população (seleção por torneio)
    private Chromosome selectParent(List<Chromosome> population) {
        Random rand = new Random();
        Chromosome best = population.get(rand.nextInt(POPULATION_SIZE));
        for (int i = 0; i < 3; i++) { // Torneio com 3 competidores
            Chromosome competitor = population.get(rand.nextInt(POPULATION_SIZE));
            if (competitor.fitness > best.fitness) {
                best = competitor;
            }
        }
        return best;
    }

    // Realiza o cruzamento de um ponto entre dois cromossomos
    private Chromosome crossover(Chromosome parent1, Chromosome parent2) {
        Random rand = new Random();
        int crossoverPoint = rand.nextInt(parent1.path.size());
        List<Point> newPath = new ArrayList<>();
        newPath.addAll(parent1.path.subList(0, crossoverPoint));
        newPath.addAll(parent2.path.subList(crossoverPoint, parent2.path.size()));
        return new Chromosome(newPath);
    }

    // Realiza a mutação em um cromossomo
    private void mutate(Chromosome chrom) {
        Random rand = new Random();
        for (Point p : chrom.path) {
            if (rand.nextDouble() < MUTATION_RATE) {
                p.x = rand.nextDouble() * GeneticAlgorithmBot.conf.getWidth(); // Ajuste conforme necessário
                p.y = rand.nextDouble() * GeneticAlgorithmBot.conf.getHeight(); // Ajuste conforme necessário
            }
        }
    }

    // Obtém o melhor cromossomo da população
    private Chromosome getBestChromosome(List<Chromosome> population) {
        Chromosome best = population.get(0);
        for (Chromosome chrom : population) {
            if (chrom.fitness > best.fitness) {
                best = chrom;
            }
        }
        return best;
    }
}