package algoritmoGenetico;

import java.util.List;
import java.util.Random;

import impl.Point;
import interf.IPoint;

public class Solution implements Comparable<Solution> {

    private List<IPoint> pontos; // o conteúdo da solução
    private int totalColisoes = 0; // total de letras iguais durante a funcao de fitness

    // TODO: FAZER O CONSTRUTOR DE COPIA
    public Solution(Solution sol) {

    }

    // TODO: FAZER O CONSTRUTOR PRINCIPAL, QUE CRIA UMA SOLUÇÃO (LISTA DE PONTOS
    // PERCORRIDOS)
    public Solution() {

    }

    // TODO: FAZER A FUNCAO FITNESS QUE ESCOLHE O CAMINHO COM MENOS COLISOES
    // POSSSIVEL
    public int getFitnessFunction() {

        return totalColisoes;
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
    public int compareTo(Solution o) {
        try {
            if (this.getFitnessFunction() > o.getFitnessFunction()) {
                return 1;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            if (this.getFitnessFunction() < o.getFitnessFunction()) {
                return -1;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return 0;
    }

    @Override
    public String toString() {
        return "Solution [pontos=" + pontos + ", totalColisoes=" + totalColisoes + ", getFitnessFunction()="
                + getFitnessFunction() + "]";
    }
}
