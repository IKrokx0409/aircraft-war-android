package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.strategy.CircularShoot;

public class FirePlusProp extends AbstractProp {
    public FirePlusProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft hero) {
        System.out.println("SuperFireSupply active!");
        hero.setStrategy(new CircularShoot());
        this.vanish();
    }
}