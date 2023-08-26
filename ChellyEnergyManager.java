package chelly;

import robocode.ScannedRobotEvent;

public class ChellyEnergyManager {

    static double getBulletPower(ScannedRobotEvent e) {
        double power = 1.5;

        if (e.getDistance() < 100) {
            power = 3;
        } else if (e.getDistance() < 300) {
            power = 2;
        }

        return power;
    }
}
