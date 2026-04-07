package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

public class BombProp extends AbstractProp {

    private BombListener listener;

    public BombProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    public void setBombListener(BombListener listener) {
        this.listener = listener;
    }

    @Override
    public void activate(HeroAircraft hero) {
        if (listener != null) {
            listener.onBombExplode();
        }
        this.vanish();
    }
}