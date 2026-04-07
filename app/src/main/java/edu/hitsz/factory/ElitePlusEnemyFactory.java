package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemyAircraft;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.basic.AbstractFlyingObject;


public class ElitePlusEnemyFactory extends EnemyFactory {
    @Override
    public AbstractEnemyAircraft createEnemy() {
        return new ElitePlusEnemy(
                (int) (Math.random() * (AbstractFlyingObject.WINDOW_WIDTH - ImageManager.ELITE_PLUS_ENEMY_IMAGE.getWidth())),
                (int) (Math.random() * AbstractFlyingObject.WINDOW_HEIGHT * 0.05),
                (int) (8 * speedMultiplier),
                (int) (7 * speedMultiplier),
                90
        );
    }
}