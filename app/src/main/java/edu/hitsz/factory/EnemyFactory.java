package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemyAircraft;

public abstract class EnemyFactory {

    /** 敌机速度倍率，由 Game 根据难度统一设置 */
    protected double speedMultiplier = 1.0;

    public void setSpeedMultiplier(double multiplier) {
        this.speedMultiplier = multiplier;
    }

    public abstract AbstractEnemyAircraft createEnemy();
}