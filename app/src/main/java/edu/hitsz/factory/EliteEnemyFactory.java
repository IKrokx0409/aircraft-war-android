package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemyAircraft;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.basic.AbstractFlyingObject;


public class EliteEnemyFactory extends EnemyFactory {
    @Override
    public AbstractEnemyAircraft createEnemy() {
        return new EliteEnemy(
                (int) (Math.random() * (AbstractFlyingObject.WINDOW_WIDTH - ImageManager.ELITE_ENEMY_IMAGE.getWidth())),
                (int) (Math.random() * AbstractFlyingObject.WINDOW_HEIGHT * 0.05),
                (int) (5 * speedMultiplier), (int) (5 * speedMultiplier), 60);
    }
}