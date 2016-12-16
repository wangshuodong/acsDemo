package com.cmiot.rms.services.template.service;

import redis.clients.jedis.ShardedJedis;

/**
 * Created by wangzhen on 2016/2/17.
 */
public interface RedisDataSource {
    /**
     * 取得redis的客户端，可以执行命令了
     *
     * @return
     */
    public abstract ShardedJedis getRedisClient();

    /**
     * 将资源返还给pool
     *
     * @param shardedJedis
     */
    public void returnResource(ShardedJedis shardedJedis);

    /**
     * 出现异常后，将资源返还给pool （其实不需要第二个方法）
     *
     * @param shardedJedis
     * @param broken
     */
    public void returnResource(ShardedJedis shardedJedis, boolean broken);
}
