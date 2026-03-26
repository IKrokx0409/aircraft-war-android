package edu.hitsz.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.FireProp;

public class FirePropFactory extends PropFactory {
    @Override
    public AbstractProp createProp(int locationX, int locationY) {
        return new FireProp(locationX, locationY, 0, 10);
    }
}