package edu.hitsz.aircraft;


import edu.hitsz.prop.AbstractProp;
import edu.hitsz.basic.AbstractFlyingObject;

import java.util.List;

/**
 * 敌机抽象类
 *
 * @author hitsz
 */
public abstract class AbstractEnemyAircraft extends AbstractAircraft {

    public AbstractEnemyAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public void forward() {
        super.forward();
        if (locationY >= AbstractFlyingObject.WINDOW_HEIGHT) {
            vanish();
        }
    }

    public abstract List<AbstractProp> dropProps();
}