package algoritmoGenetico;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import impl.Point;
import interf.IPoint;

public class Solution implements Comparable<Solution> {

    private ArrayList<IPoint> pontosSolucao; // o conteúdo da solução
    private int pontosIntermedios = 0; // total de letras iguais durante a funcao de fitness

    // TODO: FAZER O CONSTRUTOR DE COPIA
    public Solution(Solution sol) {

    }

    public Solution(ArrayList<IPoint> pontos) {
        this.pontosSolucao = pontos;
        this.pontosIntermedios = this.pontosSolucao.size() - 2;
    }

    // TODO: FAZER O CONSTRUTOR PRINCIPAL, QUE CRIA UMA SOLUÇÃO (LISTA DE PONTOS
    // PERCORRIDOS)
    public Solution() {
        this.pontosSolucao = new ArrayList<IPoint>();
    }

    public ArrayList<IPoint> getPoints() {
        return pontosSolucao;
    }

    public int getFitnessFunction() {

        return this.pontosIntermedios;
    }

    // TODO: FAZER UM GRAFICO DE EVOLUCAO DE CADA AMOSTRA DE POPULAÇÃO

    // TODO
    /**
     * implementar a mutação na solução atual.
     */
    public void mutate() {

    }

    // TODO
    /**
     * realizar o cruzamento entre esta solução e outra solução
     */
    public Solution[] cross(Solution mae) {
        Random random = new Random();

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
