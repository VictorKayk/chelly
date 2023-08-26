package chelly;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;

import java.awt.*;

/**
 * Chelly: robo criado por Victor Kayk
 *
 * Estrategias:
 * 	Targeting: Guess Factor targeting
 * 	Movement: Minimum Risk Movement
 * 	Radar: Radar preso no inimigo
 *
 **/

public class Chelly extends AdvancedRobot {
    private ChellyEyes eyes;
    private ChellyGun gun;
    private ChellyBody body;

    public void run() {
        initColors();
        initComponents();

        gun.init();

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        do {
            eyes.init();
            execute();
        } while (true);
    }

    public void initColors() {
        setColors(Color.CYAN, Color.MAGENTA, Color.PINK);
    }

    public void initComponents() {
        if (eyes == null) {
            eyes = new ChellyEyes(this);
        }

        if (gun == null) {
            gun = new ChellyGun(this);
        }

        if (body == null) {
            body = new ChellyBody(this);
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        eyes.scan(e);
        gun.aimAndShoot(e);
        body.onScannedRobot(e);
    }

    public void onWin(WinEvent e) {
        body.victoryDance();
    }

    public void onPaint(Graphics2D g) {
        body.onPaint(g);
    }
}
