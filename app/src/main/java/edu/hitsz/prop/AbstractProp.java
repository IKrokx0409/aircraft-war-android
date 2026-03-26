package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.basic.AbstractFlyingObject;


public abstract class AbstractProp extends AbstractFlyingObject {

    public AbstractProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void forward() {
        super.forward();
        if (speedY > 0 && locationY >= edu.hitsz.basic.AbstractFlyingObject.WINDOW_HEIGHT) {
            // 向下飞行出界
            vanish();
        } else if (speedY < 0 && locationY <= 0) {
            // 向上飞行出界
            vanish();
        }
    }

    public abstract void activate(HeroAircraft hero);
}