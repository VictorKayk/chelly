package chelly;

import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class ChellyEyes {
    private final Chelly robot;

    public ChellyEyes(Chelly robot) {
        this.robot = robot;
    }

    public void init() {
        if (robot.getRadarTurnRemaining() == 0.0) {
            robot.setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
        }
    }

    public void scan(ScannedRobotEvent e) {
        // Calcula a exata localização do inimigo
        double absBearing = robot.getHeadingRadians() + e.getBearingRadians();
        // Calcula o angulo que o radar deve virar para mirar no inimigo
        double turnToEnemy = Utils.normalRelativeAngle(absBearing - robot.getRadarHeadingRadians()) * 1.5;
        // Calcula o angulo extra que o radar deve virar para não perder o inimigo de vista
        double extraTurn = Math.atan(36.0 / e.getDistance()) * (turnToEnemy >= 0 ? 1 : -1);

        robot.setTurnRadarRightRadians(turnToEnemy + extraTurn); // Vira o radar para o inimigo
    }
}
