package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.strategy.ScatterShoot;

public class FireProp extends AbstractProp {

    public FireProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft hero) {
        // todo: FireProb实际功能
        System.out.println("FireSupply active!");
        hero.setStrategy(new ScatterShoot());
        this.vanish();
    }
}