package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.factory.*;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.strategy.CircularShoot;

import java.util.LinkedList;
import java.util.List;

public class BossEnemy extends AbstractEnemyAircraft {

//    private final int shootNum = 20;
//    private int power = 30;

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.power = 30;
        this.direction = 1;
        this.setStrategy(new CircularShoot());
    }

    @Override
    public void forward() {
        super.forward();
        // 不向下移动
        this.speedY = 0;
    }

//    @Override
//    public List<BaseBullet> shoot() {
//        List<BaseBullet> res = new LinkedList<>();
//        int x = this.getLocationX();
//        int y = this.getLocationY();
//        int speed = 10;
//        // 环射弹道
//        for (int i = 0; i < shootNum; i++) {
//            double angle = 2 * Math.PI * i / shootNum;
//            int speedX = (int) (speed * Math.sin(angle));
//            int speedY = (int) (speed * Math.cos(angle));
//            BaseBullet bullet = new EnemyBullet(x, y, speedX, speedY, power);
//            res.add(bullet);
//        }
//        return res;
//    }

    @Override
    public List<AbstractProp> dropProps() {
        List<AbstractProp> props = new LinkedList<>();
        double r = Math.random();
        PropFactory factory;
        if (r < 0.25) {
            factory = new HpPropFactory();
        } else if (r < 0.45) {
            factory = new FirePropFactory();
        } else if (r < 0.70) {
            factory = new FirePlusPropFactory();
        }else {
            factory = new BombPropFactory();
        }
        props.add(factory.createProp(this.locationX, this.locationY));
        return props;
    }
}