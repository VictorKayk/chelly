package chelly;

import chelly.utils.ChellyUtils;
import chelly.utils.Wave;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.awt.geom.Point2D;

public class ChellyGun {
    private Chelly robot;
    private static double lateralDirection;
    private static double lastEnemyVelocity;

    ChellyGun(Chelly robot) {
        this.robot = robot;
    }

    public void init() {
        lateralDirection = 1; // Inicializa a direção lateral para a direita
        lastEnemyVelocity = 0; // Inicializa a última velocidade do inimigo como 0
    }

    public void aimAndShoot(ScannedRobotEvent e) {
        double absBearing = robot.getHeadingRadians() + e.getBearingRadians();

        // Calcula a direção do inimigo com base na velocidade
        if (e.getVelocity() != 0) {
            double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing);
            lateralDirection = latVel >= 0 ? 1 : -1; // Define a direção lateral com base na velocidade
        }

        // Cria uma nova "onda" de tiro
        Wave wave = new Wave(robot);

        // Define os atributos da onda
        wave.gunLocation = new Point2D.Double(robot.getX(), robot.getY());
        Wave.targetLocation = ChellyUtils.project(wave.gunLocation, absBearing, e.getDistance());

        wave.lateralDirection = lateralDirection;
        wave.bulletPower = ChellyEnergyManager.getBulletPower(e); // Obtém a potência do tiro

        // Define os segmentos da onda
        wave.setSegmentations(e.getDistance(), e.getVelocity(), lastEnemyVelocity);

        lastEnemyVelocity = e.getVelocity(); // Atualiza a última velocidade do inimigo

        wave.bearing = absBearing;

        // Ajusta o ângulo do canhão para apontar para a posição mais provável do inimigo
        robot.setTurnGunRightRadians(
                Utils.normalRelativeAngle(absBearing - robot.getGunHeadingRadians() + wave.mostVisitedBearingOffset())
        );

        // Dispara se o canhão estiver pronto e a energia for suficiente
        if (robot.getGunHeat() == 0 && robot.getEnergy() > wave.bulletPower) {
            robot.setFire(wave.bulletPower); // Dispara com a potência calculada
        }

        // Adiciona o evento personalizado (a onda) se a energia for suficiente
        if (robot.getEnergy() >= 2) {
            robot.addCustomEvent(wave);
        }
    }
}
