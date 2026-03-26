package edu.hitsz.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.HpProp;

public class HpPropFactory extends PropFactory {
    @Override
    public AbstractProp createProp(int locationX, int locationY) {
        return new HpProp(locationX, locationY, 0, 10);
    }
}