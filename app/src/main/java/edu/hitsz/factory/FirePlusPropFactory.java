package edu.hitsz.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.FirePlusProp;

public class FirePlusPropFactory extends PropFactory{
    public AbstractProp createProp(int locationX, int locationY) {
        return new FirePlusProp(locationX, locationY, 0, 10);
    }
}
