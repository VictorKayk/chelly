package chelly;

import chelly.utils.Point;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChellyBody {
    private final Chelly robot;
    private final List<Point> candidatePoints = new ArrayList<>();
    private Point bestPoint;
    private double lastEnemyEnergy = 100;

    public ChellyBody(Chelly robot) {
        this.robot = robot;
    }

    // Método para executar uma dança de vitória após vencer
    public void victoryDance() {
        robot.turnRight(15);
        while (true) {
            robot.turnLeft(35);
            robot.turnRight(35);
        }
    }

    // Método chamado quando um inimigo é escaneado
    public void onScannedRobot(ScannedRobotEvent e) {
        updateLastEnemyEnergy(e);
        minimumRiskMovement(e);
    }

    // Atualiza a energia do inimigo
    private void updateLastEnemyEnergy(ScannedRobotEvent e) {
        lastEnemyEnergy = e.getEnergy();
    }

    // Método para realizar o minimum risk movement
    private void minimumRiskMovement(ScannedRobotEvent e) {
        generateCandidatePoints(e);
        findBestPoint();

        if (bestPoint != null) {
            moveToBestPoint();
        }
    }

    // Verifica se um ponto está dentro dos limites do campo de batalha
    private boolean isPointWithinBounds(double x, double y) {
        double fieldWidth = robot.getBattleFieldWidth();
        double fieldHeight = robot.getBattleFieldHeight();
        return x >= 0 && x <= fieldWidth && y >= 0 && y <= fieldHeight;
    }

    // Gera pontos candidatos em torno do robô
    private void generateCandidatePoints(ScannedRobotEvent e) {
        candidatePoints.clear();
        double interval = 2 * Math.PI / 64; // Intervalo de ângulo para 128 pontos
        double maxDistance = 128; // Distância máxima para pontos candidatos

        for (double angle = 0; angle < 2 * Math.PI; angle += interval) {
            double newX = robot.getX() + Math.sin(angle) * maxDistance;
            double newY = robot.getY() + Math.cos(angle) * maxDistance;

            if (isPointWithinBounds(newX, newY)) {
                double risk = calculateRisk(newX, newY, e);
                candidatePoints.add(new Point(newX, newY, risk));
            }
        }
    }

    private double calculateRisk(double x, double y, ScannedRobotEvent e) {
        // Calcula o risco total considerando fatores do inimigo, balas e paredes
        double enemyRisk = calculateEnemyRisk(x, y, e);
        double bulletRisk = calculateBulletRisk(x, y, e);
        double wallRisk = calculateWallRisk(x, y);

        // Pode ajustar esses pesos para equilibrar os fatores
        double enemyWeight = 1;
        double bulletWeight = 1;
        double wallWeight = 1;

        return enemyWeight * enemyRisk + bulletWeight * bulletRisk + wallWeight * wallRisk;
    }

    private double calculateEnemyRisk(double x, double y, ScannedRobotEvent e) {
        // Calcula o risco relacionado ao inimigo
        double enemyDistance = e.getDistance();

        double maxDistanceToEnemy = 300; // Distância máxima considerada

        double enemyX = robot.getX() + Math.sin(e.getBearingRadians() + robot.getHeadingRadians()) * enemyDistance;
        double enemyY = robot.getY() + Math.cos(e.getBearingRadians() + robot.getHeadingRadians()) * enemyDistance;

        double pointDistanceToEnemy = Math.hypot(x - enemyX, y - enemyY);

        double enemyRisk = 0.0;

        if (pointDistanceToEnemy <= maxDistanceToEnemy) {
            double normalizedDistance = (maxDistanceToEnemy - pointDistanceToEnemy) / maxDistanceToEnemy;
            enemyRisk = normalizedDistance;
        }

        return enemyRisk;
    }

    private double calculateBulletRisk(double x, double y, ScannedRobotEvent e) {
        // Calcula o risco relacionado às balas inimigas
        double bulletRisk = 0.0;

        double bulletSpeed = Rules.getBulletSpeed(lastEnemyEnergy - e.getEnergy()); // Velocidade da bala do inimigo

        long bulletTravelTime = (long) (e.getDistance() / bulletSpeed); // Tempo de viagem da bala

        double bulletX = robot.getX() + Math.sin(e.getBearingRadians() + robot.getHeadingRadians()) * bulletSpeed * bulletTravelTime;
        double bulletY = robot.getY() + Math.cos(e.getBearingRadians() + robot.getHeadingRadians()) * bulletSpeed * bulletTravelTime;

        double pointDistanceToBullet = Math.hypot(x - bulletX, y - bulletY); // Distância entre bala estimada e ponto

        if (pointDistanceToBullet <= bulletSpeed * bulletTravelTime) {
            bulletRisk = (bulletSpeed * bulletTravelTime - pointDistanceToBullet) / (bulletSpeed * bulletTravelTime);
        }

        return bulletRisk;
    }

    private double calculateWallRisk(double x, double y) {
        // Calcula o risco relacionado às paredes
        double fieldWidth = robot.getBattleFieldWidth();
        double fieldHeight = robot.getBattleFieldHeight();

        double minDistanceToWall = Math.min(Math.min(x, fieldWidth - x), Math.min(y, fieldHeight - y));

        double safeDistance = 100;

        double wallRisk = 0.0;

        if (minDistanceToWall <= safeDistance) {
            wallRisk = (safeDistance - minDistanceToWall) / safeDistance;
        }

        return wallRisk;
    }


    // Encontra o ponto candidato com menor risco
    private void findBestPoint() {
        double bestRisk = Double.MAX_VALUE;
        bestPoint = null;

        for (Point point : candidatePoints) {
            if (point.getRisk() < bestRisk) {
                bestRisk = point.getRisk();
                bestPoint = point;
            }
        }
    }

    // Move-se para o ponto de menor risco
    private void moveToBestPoint() {
        double bestX = bestPoint.getX();
        double bestY = bestPoint.getY();

        double angleToBestPoint = Math.atan2(bestX - robot.getX(), bestY - robot.getY());
        double distanceToBestPoint = Math.hypot(bestX - robot.getX(), bestY - robot.getY());

        robot.setTurnRightRadians(Utils.normalRelativeAngle(angleToBestPoint - robot.getHeadingRadians()));
        robot.setAhead(distanceToBestPoint);
    }

    public void onPaint(Graphics2D g) {
        // Draw candidate points and best point on the screen
        for (Point point : candidatePoints) {
            int x = (int) point.getX();
            int y = (int) point.getY();

            // Calculate color based on risk (from green to red)
            int greenValue = (int) (255 * (1 - point.getRisk()));
            int redValue = (int) (255 * point.getRisk());

            // Ensure color components are within the valid range
            greenValue = Math.min(255, Math.max(0, greenValue));
            redValue = Math.min(255, Math.max(0, redValue));

            Color pointColor = new Color(redValue, greenValue, 0);

            g.setColor(pointColor);

            if (isPointWithinBounds(x, y)) {
                g.fillOval(x - 5, y - 5, 10, 10);
            }
        }

        if (bestPoint != null) {
            // Desenha o melhor ponto na tela em branco
            g.setColor(Color.WHITE);
            int x = (int) bestPoint.getX();
            int y = (int) bestPoint.getY();
            if (isPointWithinBounds(x, y)) {
                g.fillOval(x - 5, y - 5, 10, 10);
            }
        }
    }
}