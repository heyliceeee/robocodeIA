package algoritmoGenetico;

import java.util.ArrayList;
import java.util.Random;
import impl.Point;
import interf.IPoint;
import sampleRobots.SirKazzio;

public class Solution implements Comparable<Solution> {

    private ArrayList<IPoint> pontosSolucao; // o conteúdo da solução
    private int pontosIntermedios = 0; // total de letras iguais durante a funcao de fitness
    private double danoLevado = -0.1;

    /**
     * construtor de copia
     * cria uma deep copy do objeto recebido como parametro, criando uma nova
     * instancia do Point e copia para la os pontos um a um
     * 
     * @param sol
     */
    public Solution(Solution sol) {
        this.pontosSolucao = new ArrayList<IPoint>(sol.getPoints().size());

        for (int i = 0; i < sol.getPoints().size(); i++) {

            this.pontosSolucao.add(sol.getPoints().get(i));
        }

        this.pontosIntermedios = this.pontosSolucao.size() - 2;
    }

    public Solution(ArrayList<IPoint> pontos) {
        this.pontosSolucao = pontos;
        this.pontosIntermedios = this.pontosSolucao.size() - 2;
    }

    public ArrayList<IPoint> getPoints() {
        return pontosSolucao;
    }

    public int getFitnessFunction() {

        double pesoDanoLevado = 0.8; // Peso do dano levado
        double pesoPontosIntermediarios = 0.2; // Peso dos pontos intermediários

        double fitnessDano = 1.0 / (1.0 + this.danoLevado); // Quanto menor o dano, melhor
        double fitnessPontos = 1.0 / (1.0 + this.pontosIntermedios); // Quanto menos pontos intermediários, melhor

        double fitnessTotal = (pesoDanoLevado * fitnessDano) + (pesoPontosIntermediarios * fitnessPontos);

        return (int) (fitnessTotal * 1000);

        // return this.danoLevado;
    }

    public double getDanoLevado() {

        return this.danoLevado;
    }

    public void setDanoLevado(double danoLevado) {
        this.danoLevado = danoLevado;
    }

    // TODO: FAZER UM GRAFICO DE EVOLUCAO DE CADA AMOSTRA DE POPULAÇÃO

    /**
     * implementar a mutação na solução atual.
     * 
     * altera uma posicao random da solucao atual pra conter um ponto gerado de
     * forma random.
     * mutationRate permite controlar quantos pontos pode alterar em cada mutacao
     * nao valida se altera multiplas vezes o mesmo ponto ou se troca pelo mesmo
     * ponto
     */
    public void mutate() {
        Random rand = new Random();

        boolean addPoints = rand.nextBoolean(); // decide random se adicionar ou remover pontos

        if (addPoints) {
            for (int i = 0; i < SirKazzio.MUTATION_RATE; i++) {

                // Adiciona um novo ponto random
                Point randomPointInventado = new Point(rand.nextInt(SirKazzio.conf.getWidth()),
                        rand.nextInt(SirKazzio.conf.getHeight())); // ponto random inventado

                pontosSolucao.add(randomPointInventado);
            }

            pontosIntermedios = pontosSolucao.size() - 2;
        } else {
            // remover um ponto random, se houver pontos suficientes na lista
            for (int i = 0; i < SirKazzio.MUTATION_RATE && !pontosSolucao.isEmpty(); i++) {
                int randomIndex = rand.nextInt(pontosSolucao.size()); // ponto a alterar random

                pontosSolucao.remove(randomIndex);
            }
        }
    }


    
    public int getPontosIntermedios() {
        return pontosIntermedios;
    }

    /**
     * realizar o cruzamento entre esta solução e outra solução
     */
    public Solution[] cross(Solution mae) {

        if (pontosSolucao.isEmpty() || mae.pontosSolucao.isEmpty()) {
            // Pelo menos uma das listas está vazia
            throw new IllegalArgumentException("Uma das listas está vazia");
        }

        Random rand = new Random();

        // gerar uma posicao random
        int pos;

        if (!pontosSolucao.isEmpty() && !mae.pontosSolucao.isEmpty()) {
            pos = rand.nextInt(Math.min(pontosSolucao.size(), mae.pontosSolucao.size()));
        } else if (!pontosSolucao.isEmpty()) {
            pos = rand.nextInt(pontosSolucao.size());
        } else if (!mae.pontosSolucao.isEmpty()) {
            pos = rand.nextInt(mae.pontosSolucao.size());
        } else {
            // Caso ambas as listas estejam vazias, você precisa decidir o que fazer aqui
            // Por exemplo, você pode lançar uma exceção ou retornar um valor padrão
            throw new IllegalArgumentException("Ambas as listas estão vazias");
        }

        Solution filho1 = new Solution(new ArrayList<>(this.pontosSolucao)); // Cria uma cópia dos pontos deste objeto
        Solution filho2 = new Solution(new ArrayList<>(mae.pontosSolucao)); // Cria uma cópia dos pontos da mãe

        for (int i = 0; i < Math.min(pontosSolucao.size(), mae.pontosSolucao.size()); i++) {
            if (i >= pos) {
                filho1.getPoints().set(i, mae.getPoints().get(i));
            }

            if (i >= pos) {
                filho2.getPoints().set(i, this.pontosSolucao.get(i));
            }
        }

        // Atualiza o número de pontos intermédios dos filhos
        filho1.pontosIntermedios = filho1.getPoints().size() - 2;
        filho2.pontosIntermedios = filho2.getPoints().size() - 2;

        return new Solution[] { filho1, filho2 };
    }

    /**
     * ordena as solucoes com base em sua funcao de fitness
     * 
     * @param o the object to be compared.
     * @return
     */
    @Override
    public int compareTo(Solution other) {
        return Integer.compare(other.getFitnessFunction(), this.getFitnessFunction());
    }

    @Override
    public String toString() {
        return "Solution [pontos =" + pontosSolucao
                + ", funcao fitness = " + getFitnessFunction()
                + ", n0 pontos intermediarios = " + getPontosIntermedios()
                + ", dano levado = " + getDanoLevado()
                + "]";
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Solution)) {
            return false;
        }
        Solution other = (Solution) obj;
        return this.pontosSolucao.equals(other.pontosSolucao);
    }

}
