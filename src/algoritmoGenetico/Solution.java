package algoritmoGenetico;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import impl.Point;
import interf.IPoint;
import sampleRobots.SirKazzio;

public class Solution implements Comparable<Solution> {

    private ArrayList<IPoint> pontosSolucao; // o conteúdo da solução
    private int pontosIntermedios = 0; // total de letras iguais durante a funcao de fitness

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

        return this.pontosIntermedios;
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

    /**
     * realizar o cruzamento entre esta solução e outra solução
     */
    public Solution[] cross(Solution mae) {
        Random rand = new Random();

        int pos = (!pontosSolucao.isEmpty()) ? rand.nextInt(pontosSolucao.size()) : 0; // gerar uma posicao random

        Solution filho1 = new Solution(this.pontosSolucao); // criar um novo filho 1 com os pontos deste objeto
        Solution filho2 = new Solution(mae.pontosSolucao); // criar um novo filho 2 com os pontos da mãe

        for (int i = pos; i < Math.min(pontosSolucao.size(), mae.pontosSolucao.size()); i++) // preencher o filho 1
        {
            filho1.getPoints().set(i, mae.getPoints().get(i));

            filho1.pontosIntermedios = filho1.getPoints().size() - 2;
        }

        for (int i = pos; i < Math.min(mae.pontosSolucao.size(), pontosSolucao.size()); i++) // preencher o filho 2
        {
            filho2.getPoints().set(i, this.pontosSolucao.get(i));

            filho2.pontosIntermedios = filho1.getPoints().size() - 2;
        }

        Solution[] filhos = new Solution[] { filho1, filho2 };

        return filhos;
    }

    /**
     * ordena as solucoes com base em sua funcao de fitness
     * 
     * @param o the object to be compared.
     * @return
     */
    @Override
    public int compareTo(Solution other) {
        return Integer.compare(this.getFitnessFunction(), other.getFitnessFunction());
    }

    @Override
    public String toString() {
        return "Solution [pontos=" + pontosSolucao + ", getFitnessFunction()=" + getFitnessFunction() + "]";
    }
}
