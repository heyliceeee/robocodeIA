package problemaB;

import java.awt.Rectangle;
import java.util.List;

import interf.IPoint;
import sampleRobots.SirKazzio;
import utils.Utils;

public class Cromossomo implements Comparable<Cromossomo> {
    public List<IPoint> caminho;
    public double fitnessFunction;

    public Cromossomo(List<IPoint> caminho) {
        this.caminho = caminho;
        this.fitnessFunction = calcularFitnessFunction();
    }

    public List<IPoint> getCaminho() {
        return caminho;
    }

    public void setCaminho(List<IPoint> caminho) {
        this.caminho = caminho;
    }

    public double getFitnessFunction() {
        return fitnessFunction;
    }

    public void setFitnessFunction(double fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
    }

    /**
     * calcula a fitness function com base nos pontos intermedios do caminho e em
     * como ele evita obstaculos
     * 
     * @return
     */
    public double calcularFitnessFunction() {
        double fitnessFunction = 0.0;

        for (int i = 1; i < caminho.size(); i++) {
            IPoint p1 = caminho.get(i - 1);
            IPoint p2 = caminho.get(i);

            int p1X = p1.getX();
            int p1Y = p1.getY();
            int p2X = p2.getX();
            int p2Y = p2.getY();

            fitnessFunction -= Utils.getDistance(p1X, p1Y, p2X, p2Y);

            for (Rectangle obstaculo : SirKazzio.obstaculos) {
                if (obstaculo.contains(p2X, p2Y)) { // o robo foi contra algum obstaculo
                    fitnessFunction -= 1000;
                }
            }
        }

        return fitnessFunction;
    }

    @Override
    public int compareTo(Cromossomo o) {
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
