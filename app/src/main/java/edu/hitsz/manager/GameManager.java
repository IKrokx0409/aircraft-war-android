package edu.hitsz.manager;

/**
 * 游戏管理器接口，抽象单机和联机模式的共同操作
 */
public interface GameManager {
    /**
     * 初始化游戏管理器（建立连接等）
     */
    void initialize();

    /**
     * 重置游戏状态（保留连接）
     */
    void reset();

    /**
     * 清理游戏资源（关闭连接等）
     */
    void cleanup();

    /**
     * 判断是否为联机模式
     */
    boolean isOnline();

    /**
     * 获取游戏模式名称
     */
    String getModeName();
}