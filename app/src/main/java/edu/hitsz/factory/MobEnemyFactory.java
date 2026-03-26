package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemyAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.basic.AbstractFlyingObject;


public class MobEnemyFactory extends EnemyFactory {
    @Override
    public AbstractEnemyAircraft createEnemy() {
        return new MobEnemy(
                (int) (Math.random() * (AbstractFlyingObject.WINDOW_WIDTH - ImageManager.MOB_ENEMY_IMAGE.getWidth())),
                (int) (Math.random() * AbstractFlyingObject.WINDOW_HEIGHT * 0.05),
                0, 10, 30);
    }
}