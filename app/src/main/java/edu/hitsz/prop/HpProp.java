package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

public class HpProp extends AbstractProp {

    public HpProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft hero) {
        System.out.println("HpSupply active!");
        hero.increaseHp(30); // 恢复30点生命
        this.vanish();
    }
}