package problemaB;

import java.awt.Rectangle;
import java.util.List;

import interf.IPoint;
import sampleRobots.SirKazzio;
import utils.Utils;

public class Cromossomo {
    public List<IPoint> caminho;
    public double fitnessFunction;

    public Cromossomo(List<IPoint> caminho) {
        this.caminho = caminho;
        this.fitnessFunction = calcularFitnessFunction();
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


            fitnessFunction -= Utils.getDistance((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());

            for (Rectangle obstaculo : SirKazzio.obstaculos) {
                if (obstaculo.contains(p2.getX(), p2.getY())) { // o robo foi contra algum obstaculo
                    fitnessFunction -= 1000;
                }
            }
        }

        return fitnessFunction;
    }
}
