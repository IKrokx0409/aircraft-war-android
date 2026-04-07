package edu.hitsz.prop;

/**
 * 炸弹爆炸事件的观察者接口。
 * BombProp 触发，Game 实现并处理具体爆炸效果。
 */
public interface BombListener {
    void onBombExplode();
}
