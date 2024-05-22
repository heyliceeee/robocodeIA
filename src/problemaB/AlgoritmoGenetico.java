package problemaB;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import impl.UIConfiguration;
import interf.IPoint;
import sampleRobots.SirKazzio;

public class AlgoritmoGenetico {

    public static int geracaoAtual = 0;

    /**
     * selecionar pai e mae
     * 
     * @param populacao
     * @return
     */
    public static List<Cromossomo> selecionarPais(Populacao populacao) {
        List<Cromossomo> pais = new ArrayList<>();

        Collections.sort(populacao.cromossomos, Comparator.comparingDouble(c -> c.fitnessFunction));

        for (int i = 0; i < SirKazzio.POP_HEREDITARY; i++) {
            pais.add(populacao.cromossomos.get(i));
        }

        return pais;
    }

    /**
     * cruzamento de caminhos, ou seja pegar em 2 caminhos e trocar
     * 
     * @param mae
     * @param pai
     * @return
     */
    public static Cromossomo cruzamento(Cromossomo mae, Cromossomo pai) {
        Random rand = new Random();
        List<IPoint> caminhoFilho = new ArrayList<>();

        int pontoDeCruzamento = rand.nextInt(mae.caminho.size());

        for (int i = 0; i < pontoDeCruzamento; i++) {
            caminhoFilho.add(mae.caminho.get(i));
        }

        for (int i = pontoDeCruzamento; i < pai.caminho.size(); i++) {
            caminhoFilho.add(pai.caminho.get(i));
        }

        return new Cromossomo(caminhoFilho);
    }

    /**
     * mutação de caminhos, ou seja, seleciona um ponto random e altera ligeiramente
     * 
     * @param cromossomo
     * @param conf
     */
    public static void mutacao(Cromossomo cromossomo, UIConfiguration conf) {
        Random rand = new Random();

        for (IPoint ponto : cromossomo.caminho) {
            if (rand.nextInt(100) < SirKazzio.MUTATION_RATE) {
                ponto.setX(rand.nextInt(conf.getWidth()));
                ponto.setY(rand.nextInt(conf.getHeight()));
            }
        }

        cromossomo.fitnessFunction = cromossomo.calcularFitnessFunction();
    }

    public static Populacao evoluirPopulacao(Populacao populacao, UIConfiguration conf, List<Rectangle> obstaculos) {
        // Print para mostrar a geração atual
        System.out.println("[evoluirPopulacao] Geração: " + geracaoAtual);

        List<Cromossomo> novosCromossomos = new ArrayList<>();
        List<Cromossomo> pais = selecionarPais(populacao);

        for (int i = 0; i < populacao.cromossomos.size(); i++) {
            Cromossomo mae = pais.get(new Random().nextInt(pais.size()));
            Cromossomo pai = pais.get(new Random().nextInt(pais.size()));

            Cromossomo filho = cruzamento(mae, pai);

            mutacao(filho, conf);

            novosCromossomos.add(filho);
        }

        Populacao novaPopulacao = new Populacao(0, conf);
        novaPopulacao.cromossomos = novosCromossomos;

        // Print para mostrar os melhores caminhos encontrados na geração atual
        Cromossomo melhorCaminho = novaPopulacao.getMelhor();
        System.out.println("[evoluirPopulacao] Melhor caminho na geração " + geracaoAtual + ": "
                + melhorCaminho.caminho.toString());

        // Incrementa o número da geração
        geracaoAtual++;

        return novaPopulacao;
    }
}
