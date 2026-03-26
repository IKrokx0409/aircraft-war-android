package edu.hitsz.factory;

import edu.hitsz.prop.AbstractProp;

public abstract class PropFactory {
    public abstract AbstractProp createProp(int locationX, int locationY);
}