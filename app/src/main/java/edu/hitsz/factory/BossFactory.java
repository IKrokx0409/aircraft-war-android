package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemyAircraft;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.basic.AbstractFlyingObject;


public class BossFactory extends EnemyFactory {
    @Override
    public AbstractEnemyAircraft createEnemy() {
        return createEnemy(1000);
    }

    /** 以指定 HP 创建 Boss，供 Hard 模式波次递增血量使用 */
    public AbstractEnemyAircraft createEnemy(int hp) {
        return new BossEnemy(
                AbstractFlyingObject.WINDOW_WIDTH / 2,
                100,
                5,
                0,
                hp
        );
    }
}