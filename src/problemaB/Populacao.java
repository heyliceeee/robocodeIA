package problemaB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import impl.Point;
import impl.UIConfiguration;
import interf.IPoint;

public class Populacao {
    public List<Cromossomo> cromossomos;

    public Populacao(int tamanho, UIConfiguration conf) {
        cromossomos = new ArrayList<>();

        for (int i = 0; i < tamanho; i++) {
            cromossomos.add(gerarCromossomoRandom(conf));
        }
    }

    /**
     * gerar caminho aleatorio
     * 
     * @param conf
     * @return
     */
    private Cromossomo gerarCromossomoRandom(UIConfiguration conf) {
        Random rand = new Random();
        List<IPoint> caminho = new ArrayList<>();

        caminho.add(conf.getStart());

        int tamanho = rand.nextInt(5) + 5;

        for (int i = 0; i < tamanho; i++) {
            caminho.add(new Point(rand.nextInt(conf.getWidth()), rand.nextInt(conf.getHeight())));
        }

        caminho.add(conf.getEnd());

        return new Cromossomo(caminho);
    }

    /**
     * cromossomo com melhor fitness (menor caminho e desviou mais dos obstaculos)
     * 
     * @return
     */
    public Cromossomo getMelhor() {
        return Collections.max(cromossomos, Comparator.comparingDouble(c -> c.fitnessFunction));
    }
}
