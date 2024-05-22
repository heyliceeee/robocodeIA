package problemaB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import impl.Point;
import impl.UIConfiguration;
import interf.IPoint;
import sampleRobots.SirKazzio;

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

        // Determinar as dimensões do mapa
        int mapWidth = conf.getWidth();
        int mapHeight = conf.getHeight();

        for (int i = 0; i < tamanho; i++) {
            int x = rand.nextInt(mapWidth);
            int y = rand.nextInt(mapHeight);

            // Verificar se a coordenada está dentro da margem de segurança em relação às
            // paredes
            if (x < SirKazzio.MIN_DISTANCIA_PAREDE) {
                x = SirKazzio.MIN_DISTANCIA_PAREDE; // Ajustar para manter a margem de segurança em relação à parede
                                                    // esquerda
            } else if (x > mapWidth - SirKazzio.MIN_DISTANCIA_PAREDE) {
                x = mapWidth - SirKazzio.MIN_DISTANCIA_PAREDE; // Ajustar para manter a margem de segurança em relação à
                                                               // parede direita
            }
            if (y < SirKazzio.MIN_DISTANCIA_PAREDE) {
                y = SirKazzio.MIN_DISTANCIA_PAREDE; // Ajustar para manter a margem de segurança em relação à parede
                                                    // superior
            } else if (y > mapHeight - SirKazzio.MIN_DISTANCIA_PAREDE) {
                y = mapHeight - SirKazzio.MIN_DISTANCIA_PAREDE; // Ajustar para manter a margem de segurança em relação
                                                                // à parede inferior
            }

            caminho.add(new Point(x, y));
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
        return Collections.max(cromossomos, Comparator.comparingDouble(c -> c.getFitnessFunction()));
    }
}
