package sampleRobots;

import robocode.*;

import java.awt.geom.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReconhecimentoRobot extends AdvancedRobot {

    private class Dados {

        String nome; //Nome do robo do inimigo
        Double distancia; //distancia a que o robot se encontra
        Double velocidade; //velocidade a que o robot inimigo se desloca
        Double angulo;
        Double energia; //energia do robot inimigo
        Double cordenadaX;//cordenada x a que o robot se encontra
        Double cordenadaY;//distancia Y que o robot se encontra

        public Dados(String nome, Double distancia, Double velocidade, Double angulo,Double energia ,Double cordenadaX, Double cordenadaY) {
            this.nome = nome;
            this.distancia = distancia;
            this.velocidade = velocidade;
            this.angulo = angulo;
            this.energia = energia;
            this.cordenadaX = cordenadaX;
            this.cordenadaY = cordenadaY;

        }
    }

    RobocodeFileOutputStream fw;

    HashMap<Bullet, Dados> balasNoAr = new HashMap<>();

    @Override
    public void run() {
        super.run();
        
        File dataFile = this.getDataFile("reconhecimento.csv");
        boolean isEmpty = true;
        
        try {
            BufferedReader buffer = new BufferedReader(new FileReader(dataFile));
            isEmpty = buffer.readLine() == null;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReconhecimentoRobot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ReconhecimentoRobot.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            fw = new RobocodeFileOutputStream(dataFile.getAbsolutePath(), true);
            System.out.println("Writing to: " + fw.getName());
            System.out.println("Existe:" + isEmpty);
            
            if (isEmpty) {
                fw.write(("nome,distancia,velocidade,angulo,energia,cordenadaX,cordenadaY,hit\n").getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            setAhead(100);
            setTurnLeft(100);
            Random rand = new Random();
            setAllColors(new Color(rand.nextInt(3), rand.nextInt(3), rand.nextInt(3)));
            execute();
        }

    }

    @Override
    // Quando o radar detecta outro robo
    public void onScannedRobot(ScannedRobotEvent event) {
        super.onScannedRobot(event);

        Point2D.Double coordinates = utils.Utils.getEnemyCoordinates(this, event.getBearing(), event.getDistance());
        System.out.println("Enemy " + event.getName() + " spotted at " + coordinates.x + "," + coordinates.y + "\n");
        Bullet b = fireBullet(3);

        if (b != null) {
            System.out.println("Firing at " + event.getName());
            balasNoAr.put(b, new Dados(event.getName(), event.getVelocity(), event.getDistance(), event.getBearing(),event.getEnergy() ,coordinates.x, coordinates.y));
        } else {
            System.out.println("Cannot fire right now...");
        }

    }

    @Override
    // Quando uma bala atinge o adversario
    public void onBulletHit(BulletHitEvent event) {
        super.onBulletHit(event);
        Dados d = balasNoAr.get(event.getBullet());
        try {
            //Testar se acertei em quem era suposto
            if (event.getName().equals(event.getBullet().getVictim())) {
                fw.write((d.nome + "," + d.distancia + "," + d.velocidade + "," +d.angulo+","+d.energia+","+ d.cordenadaX + "," + d.cordenadaY + ",hit\n").getBytes());
            } else {
                fw.write((d.nome + "," + d.distancia + "," + d.velocidade + "," +d.angulo+","+d.energia+","+ d.cordenadaX + "," + d.cordenadaY + ",no_hit\n").getBytes());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        balasNoAr.remove(event.getBullet());
    }

    @Override
    // Quando uma bala não atinge o alvo
    public void onBulletMissed(BulletMissedEvent event) {
        super.onBulletMissed(event);
        Dados d = balasNoAr.get(event.getBullet());
        try {
            fw.write((d.nome + "," + d.distancia + "," + d.velocidade + "," +d.angulo+","+d.energia+","+ d.cordenadaX + "," + d.cordenadaY + ",no_hit\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        balasNoAr.remove(event.getBullet());
    }

    @Override
    // Quando uma bala acerta noutra bala
    public void onBulletHitBullet(BulletHitBulletEvent event) {
        super.onBulletHitBullet(event);
        Dados d = balasNoAr.get(event.getBullet());
        try {
            fw.write((d.nome + "," + d.distancia + "," + d.velocidade + "," +d.angulo+","+d.energia+","+ d.cordenadaX + "," + d.cordenadaY + ",no_hit\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        balasNoAr.remove(event.getBullet());
    }

    @Override
    // Metodo chamado quando morre o robot
    public void onDeath(DeathEvent event) {
        super.onDeath(event);

        try {
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    // Metodo chamado quando a batalha termina
    public void onRoundEnded(RoundEndedEvent event) {
        super.onRoundEnded(event);

        try {
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
