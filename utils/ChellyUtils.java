package chelly.utils;

import java.awt.geom.Point2D;

public class ChellyUtils {
    // Calcula a velocidade da bala com base na potência
    public static double bulletVelocity(double power) {
        return 20 - 3 * power;
    }

    // Projeta uma posição a partir de uma localização, ângulo e distância
    public static Point2D project(Point2D sourceLocation, double angle, double length) {
        return new Point2D.Double(sourceLocation.getX() + Math.sin(angle) * length,
                sourceLocation.getY() + Math.cos(angle) * length);
    }

    // Calcula o ângulo absoluto entre duas posições
    public static double absoluteBearing(Point2D source, Point2D target) {
        return Math.atan2(target.getX() - source.getX(), target.getY() - source.getY());
    }

    // Garante que um valor esteja dentro de um intervalo
    public static int minMax(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
