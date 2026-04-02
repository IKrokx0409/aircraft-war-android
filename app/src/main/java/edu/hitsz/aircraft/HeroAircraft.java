package edu.hitsz.aircraft;

import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.strategy.DirectShoot;

import java.util.LinkedList;
import java.util.List;

import edu.hitsz.application.ImageManager;



/**
 * 英雄飞机，游戏玩家操控
 * @author hitsz
 */
public class HeroAircraft extends AbstractAircraft {

    private volatile static HeroAircraft instance = null;
    /**攻击方式 */

    /**
     * 子弹一次发射数量
     */
//    private int shootNum = 1;
//
//    /**
//     * 子弹伤害
//     */
//    private int power = 30;
//
//    /**
//     * 子弹射击方向 (向上发射：1，向下发射：-1)
//     */
//    private int direction = -1;

    /**
     * @param locationX 英雄机位置x坐标
     * @param locationY 英雄机位置y坐标
     * @param speedX 英雄机射出的子弹的基准速度（英雄机无特定速度）
     * @param speedY 英雄机射出的子弹的基准速度（英雄机无特定速度）
     * @param hp    初始生命值
     */
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
        // 重置生命值
        this.hp = 1000;  // 改为你的初始 hp 值

        // 重置位置到屏幕中心（假设屏幕宽高为 1080x1920）
        // 如果你有 screenWidth/screenHeight，可以用参数方式
        this.locationX = AbstractFlyingObject.WINDOW_WIDTH / 2;   // 屏幕中心 X
        this.locationY = AbstractFlyingObject.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight();

        // 重置速度
        this.speedX = 0;
        this.speedY = 0;

        //重置射击模式为直射
        this.setStrategy(new DirectShoot());
    }

    @Override
    public void forward() {
        // 英雄机由鼠标控制，不通过forward函数移动
    }

//     @Override
//    /**
//     * 通过射击产生子弹
//     * @return 射击出的子弹List
//     */
//    public List<BaseBullet> shoot() {
//        List<BaseBullet> res = new LinkedList<>();
//        int x = this.getLocationX();
//        int y = this.getLocationY() + direction*2;
//        int speedX = 0;
//        int speedY = this.getSpeedY() + direction*5;
//        BaseBullet bullet;
//        for(int i=0; i<shootNum; i++){
//            // 子弹发射位置相对飞机位置向前偏移
//            // 多个子弹横向分散
//            bullet = new HeroBullet(x + (i*2 - shootNum + 1)*10, y, speedX, speedY, power);
//            res.add(bullet);
//        }
//        return res;
//    }

    public void increaseHp(int increase) {
        hp += increase;
        if (hp > maxHp) {
            hp = maxHp;
        }
    }

}

