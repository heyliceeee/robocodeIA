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
        for (int i = 0; i < SirKazzio.MUTATION_RATE; i++) {
            Random rand = new Random();

            Point randomPointInventado = new Point(rand.nextInt(SirKazzio.conf.getWidth()),
                    rand.nextInt(SirKazzio.conf.getHeight())); // ponto aleatorio inventado

            int randomIndex = rand.nextInt(pontosSolucao.size()); // ponto a alterar aleatorio

            this.pontosSolucao.add(randomIndex, randomPointInventado);
        }
    }

    // TODO
    /**
     * realizar o cruzamento entre esta solução e outra solução
     */
    public Solution[] cross(Solution mae) {
        Random rand = new Random();

        Solution[] filhos = new Solution[] { this, mae };

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
