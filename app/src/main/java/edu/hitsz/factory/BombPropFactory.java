package edu.hitsz.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BombProp;

public class BombPropFactory extends PropFactory {
    @Override
    public AbstractProp createProp(int locationX, int locationY) {
        return new BombProp(locationX, locationY, 0, 10);
    }
}