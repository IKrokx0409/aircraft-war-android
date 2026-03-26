package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemyAircraft;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.basic.AbstractFlyingObject;


public class BossFactory extends EnemyFactory {
    @Override
    public AbstractEnemyAircraft createEnemy() {
        return new BossEnemy(
                AbstractFlyingObject.WINDOW_WIDTH / 2,
                100,
                5,
                0,
                1000
        );
    }
}