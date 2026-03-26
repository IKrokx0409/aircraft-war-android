package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.factory.*;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.strategy.ScatterShoot;

import java.util.LinkedList;
import java.util.List;

public class ElitePlusEnemy extends AbstractEnemyAircraft {

//    private int shootNum = 3;
//    private int power = 20;
//    private int direction = 1;
    public ElitePlusEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.power = 20;
        this.direction = 1;
        this.setStrategy(new ScatterShoot());
    }

//    @Override
//    public List<BaseBullet> shoot() {
//        List<BaseBullet> res = new LinkedList<>();
//        int x = this.getLocationX();
//        int y = this.getLocationY() + direction * 2;
//        int speedY = this.getSpeedY() + direction * 4;
//        // 实现散射弹道
//        for (int i = 0; i < shootNum; i++) {
//            int speedX = (i - 1) * 5;
//            BaseBullet bullet = new EnemyBullet(x, y, speedX, speedY, power);
//            res.add(bullet);
//        }
//        return res;
//    }

    @Override
    public List<AbstractProp> dropProps() {
        List<AbstractProp> props = new LinkedList<>();
        double r = Math.random();
        if (r < 0.25) {
            PropFactory factory = new HpPropFactory();
            props.add(factory.createProp(this.locationX, this.locationY));
        } else if (r < 0.45) {
            PropFactory factory = new FirePropFactory();
            props.add(factory.createProp(this.locationX, this.locationY));
        } else if (r < 0.7) {
            PropFactory factory = new BombPropFactory();
            props.add(factory.createProp(this.locationX, this.locationY));
        } else if (r < 0.9) {
            PropFactory factory = new FirePlusPropFactory();
            props.add(factory.createProp(this.locationX, this.locationY));
        }
        return props;
    }
}