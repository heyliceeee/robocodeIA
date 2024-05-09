package algoritmoGenetico;

import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GraficoJanela extends JFrame {
    private List<Solution> pontosPercorridos;

    public GraficoJanela(List<Solution> pontosPercorridos) {
        this.pontosPercorridos = pontosPercorridos;
        setSize(400, 300);
        setVisible(true);
    }

    public void paint(Graphics g) {
        super.paint(g);

        // Encontrar o valor máximo de pontosIntermedios
        int maxPontosIntermedios = 0;
        for (Solution solution : pontosPercorridos) {
            if (solution.getPontosIntermedios() > maxPontosIntermedios) {
                maxPontosIntermedios = solution.getPontosIntermedios();
            }
        }

        // Desenhar eixos
        g.drawLine(50, 250, 350, 250); // Eixo X
        g.drawLine(50, 250, 50, 50); // Eixo Y

        // Desenhar barras com base nos dados
        int barWidth = 20;
        int x = 100;
        for (Solution solution : pontosPercorridos) {
            int barHeight = (int) (solution.getPontosIntermedios() * 200.0 / maxPontosIntermedios);
            g.fillRect(x, 250 - barHeight, barWidth, barHeight);
            x += 40; // Incremento para a próxima barra
        }
    }
}
