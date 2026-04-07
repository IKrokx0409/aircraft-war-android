package edu.hitsz.aircraft;

import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.strategy.DirectShoot;

import edu.hitsz.application.ImageManager;

public class HeroAircraft extends AbstractAircraft {

    private volatile static HeroAircraft instance = null;

    public HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.power = 30;
        this.direction = -1;
        this.setStrategy(new DirectShoot());
    }

    public static HeroAircraft getInstance() {
        if (instance == null) {
            synchronized (HeroAircraft.class) {
                if (instance == null) {
                    instance = new HeroAircraft(
                            AbstractFlyingObject.WINDOW_WIDTH / 2,
                            AbstractFlyingObject.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight(),
                            0, 0, 1000);
                }
            }
        }
        return instance;
    }

    public void reset() {
        this.hp = 1000;
        this.locationX = AbstractFlyingObject.WINDOW_WIDTH / 2;
        this.locationY = AbstractFlyingObject.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight();
        this.speedX = 0;
        this.speedY = 0;
        this.setStrategy(new DirectShoot());
    }

    @Override
    public void forward() {
        // 英雄机由触摸控制，不通过 forward 移动
    }

    public void increaseHp(int increase) {
        hp += increase;
        if (hp > maxHp) {
            hp = maxHp;
        }
    }
}
