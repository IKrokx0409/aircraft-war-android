package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import java.util.LinkedList;
import java.util.List;

public class ScatterShoot implements ShootStrategy {
    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + aircraft.getDirection() * 2;
        int speedY = aircraft.getSpeedY() + aircraft.getDirection() * 4;
        int shootNum = 5;

        for (int i = 0; i < shootNum; i++) {
            int speedX = (i - 1) * 5;
            BaseBullet bullet;
            if (aircraft instanceof HeroAircraft) {
                bullet = new HeroBullet(x, y, speedX, speedY, aircraft.getPower());
            } else {
                bullet = new EnemyBullet(x, y, speedX, speedY, aircraft.getPower());
            }
            res.add(bullet);
        }
        return res;
    }
}