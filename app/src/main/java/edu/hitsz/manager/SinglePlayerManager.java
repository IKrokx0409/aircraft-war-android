package edu.hitsz.manager;

/**
 * 单机模式的游戏管理器实现
 */
public class SinglePlayerManager implements GameManager {

    @Override
    public void initialize() {
        // 单机模式无需初始化网络连接
    }

    @Override
    public void reset() {
        // 单机模式无需重置任何网络状态
    }

    @Override
    public void cleanup() {
        // 单机模式无需清理任何资源
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public String getModeName() {
        return "single";
    }
}