package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemyAircraft;

public abstract class EnemyFactory {
    public abstract AbstractEnemyAircraft createEnemy();
}