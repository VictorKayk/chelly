package chelly.utils;

import chelly.Chelly;
import robocode.Condition;
import robocode.util.Utils;

import java.awt.geom.Point2D;

public class Wave extends Condition {
    // Variável estática para armazenar a posição estimada do alvo
    public static Point2D targetLocation;

    // Atributos da onda
    public double bulletPower;
    public Point2D gunLocation;
    public double bearing;
    public double lateralDirection;

    // Constantes para segmentar os dados da onda
    public static final double MAX_DISTANCE = 1000;
    public static final int DISTANCE_INDEXES = 5;
    public static final int VELOCITY_INDEXES = 5;
    public static final int BINS = 25;
    public static final int MIDDLE_BIN = (BINS - 1) / 2;
    public static final double MAX_ESCAPE_ANGLE = 0.7;
    public static final double BIN_WIDTH = MAX_ESCAPE_ANGLE / (double)MIDDLE_BIN;

    // Matriz para armazenar os contadores de cada segmento da onda
    public static int[][][][] statBuffers = new int[DISTANCE_INDEXES][VELOCITY_INDEXES][VELOCITY_INDEXES][BINS];

    // Buffer atual para os contadores
    public int[] buffer;
    public double distanceTraveled;
    public Chelly robot;

    public Wave(Chelly robot) {
        this.robot = robot;
    }

    // Método que verifica se a onda chegou ao alvo
    public boolean test() {
        updateDistanceTraveled(); // Atualiza a distância percorrida pela onda
        if (successfulWave()) {
            buffer[currentBin()]++; // Incrementa o contador do segmento atual
            robot.removeCustomEvent(this); // Remove o evento personalizado quando a onda chega ao alvo
        }
        return false;
    }

    // Calcula o desvio de direção mais visitado
    public double mostVisitedBearingOffset() {
        return (lateralDirection * BIN_WIDTH) * (mostVisitedBin() - MIDDLE_BIN);
    }

    // Define os segmentos para os dados da onda com base na distância e velocidades
    public void setSegmentations(double distance, double velocity, double lastVelocity) {
        int distanceIndex = (int)(distance / (MAX_DISTANCE / DISTANCE_INDEXES));
        int velocityIndex = (int)Math.abs(velocity / 2);
        int lastVelocityIndex = (int)Math.abs(lastVelocity / 2);
        buffer = statBuffers[distanceIndex][velocityIndex][lastVelocityIndex];
    }

    // Atualiza a distância percorrida pela onda
    public void updateDistanceTraveled() {
        distanceTraveled += ChellyUtils.bulletVelocity(bulletPower);
    }

    // Verifica se a onda chegou ao alvo
    public boolean successfulWave() {
        return distanceTraveled > gunLocation.distance(targetLocation) - 18;
    }

    // Retorna o índice do segmento atual
    public int currentBin() {
        int bin = (int)Math.round(((Utils.normalRelativeAngle(ChellyUtils.absoluteBearing(gunLocation, targetLocation) - bearing)) /
                (lateralDirection * BIN_WIDTH)) + MIDDLE_BIN);
        return ChellyUtils.minMax(bin, 0, BINS - 1); // Garante que o índice do segmento esteja dentro dos limites
    }

    // Retorna o índice do segmento mais visitado
    public int mostVisitedBin() {
        int mostVisited = MIDDLE_BIN;
        for (int i = 0; i < BINS; i++) {
            if (buffer[i] > buffer[mostVisited]) {
                mostVisited = i;
            }
        }
        return mostVisited;
    }
}
