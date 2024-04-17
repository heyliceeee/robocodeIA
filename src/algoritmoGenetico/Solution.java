package algoritmoGenetico;

import java.util.List;
import java.util.Random;

import impl.Point;
import interf.IPoint;

public class Solution implements Comparable<Solution> {

    private List<IPoint> pontos; // o conteúdo da solução
    private int totalColisoes = 0; // total de letras iguais durante a funcao de fitness

    public Solution(Solution sol) {

    }

    public Solution() {

    }

    public int getFitnessFunction() throws Exception {

        return totalColisoes;
    }

    /**
     * implementar a mutação na solução atual.
     */
    public void mutate() {

    }

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

}
