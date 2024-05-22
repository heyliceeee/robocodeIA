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

        // Calcular o tamanho da margem de segurança em relação ao tamanho do mapa
        int margemSegurancaX = (int) (mapWidth * 0.4); // 20% da largura do mapa
        int margemSegurancaY = (int) (mapHeight * 0.4); // 20% da altura do mapa

        for (int i = 0; i < tamanho; i++) {
            int x, y;
            do {
                x = rand.nextInt(mapWidth - 2 * margemSegurancaX) + margemSegurancaX;
                y = rand.nextInt(mapHeight - 2 * margemSegurancaY) + margemSegurancaY;

                // Verificar se a coordenada está dentro da margem de segurança
            } while (estaPertoDaMargem(x, y, margemSegurancaX, margemSegurancaY, mapWidth, mapHeight));

            caminho.add(new Point(x, y));
        }

        caminho.add(conf.getEnd());

        return new Cromossomo(caminho);
    }

    // Função auxiliar para verificar se um ponto está perto da margem de segurança
    private boolean estaPertoDaMargem(int x, int y, int margemSegurancaX, int margemSegurancaY, int mapWidth,
            int mapHeight) {
        return x <= margemSegurancaX || x >= mapWidth - margemSegurancaX ||
                y <= margemSegurancaY || y >= mapHeight - margemSegurancaY;
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
