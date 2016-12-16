package com.cmiot.acs.domain.cache;

import com.cmiot.acs.common.ApplicationContextUtil;
import com.cmiot.acs.model.AbstractMethod;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZJL on 2016/11/21.
 */
public class SpringRedisUtil {

    private static RedisTemplate<String, String> redisTemplate =
            (RedisTemplate<String, String>) ApplicationContextUtil.getBean("redisTemplate");


    private static RedisTemplate<Serializable, Serializable> redisTemplateSerializable =
            (RedisTemplate<Serializable, Serializable>) ApplicationContextUtil.getBean("redisTemplate");


    /**
     * 名称为key的List尾部追加记录
     *
     * @param key
     * @param value
     * @return 记录总数
     */
    public static void leftPush(final String key, Object value) {
        final byte[] keyBytes = redisTemplateSerializable.getStringSerializer().serialize(key);
        redisTemplateSerializable.opsForList().leftPush(keyBytes, SerializeUtil.serialize(value));
    }


    /**
     * 名称为key的List尾部追加记录
     *
     * @param key
     * @param values
     */
    public static void leftPush(final String key, List<AbstractMethod> values) {
        for (Object value : values) {
            leftPush(key, value);
        }
    }


    /**
     * 返回并删除名称为key的list中的首元素
     *
     * @param key
     * @param elementType
     * @param <T>
     * @return
     */
    public static <T> T opsForList(final String key, Class<T> elementType) {
        final byte[] keyBytes = redisTemplateSerializable.getStringSerializer().serialize(key);
        if (redisTemplateSerializable.opsForList().size(keyBytes) > 0) {
            byte[] valueBytes = (byte[]) redisTemplateSerializable.opsForList().rightPop(keyBytes);
            T value = (T) SerializeUtil.unserialize(valueBytes);
            return value;
        } else {
            return null;
        }
    }


    /**
     * 删除名称为key的List中的所有元素
     *
     * @param key
     */
    public static void deleteList(final String key) {
        final byte[] keyBytes = redisTemplateSerializable.getStringSerializer().serialize(key);
        redisTemplateSerializable.delete(keyBytes);
    }


    /**
     * 如果不存在名称为key的string，则向库中添加string，名称为key
     *
     * @param key
     * @param value
     */
    public static void set(final String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }


    /**
     * 向库中添加string（名称为key，值为value）同时，设定过期时间time
     *
     * @param key
     * @param value
     */
    public static void set(final String key, String value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
    }

    /**
     * 向库中获取名称为key值
     *
     * @param key
     */
    public static String get(final String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 向库中删除名称为key的元素
     *
     * @param key
     */
    public static void delete(final String key) {
        redisTemplate.delete(key);
    }


    private static final String status = "_ST";

    public static void setSt(String key, String value) {
        set(key + status, value);
    }

    public static void setSt(String key, String value, long timeout) {
        set(key + status, value, timeout);
    }

    public static void deleteSt(final String key) {
        redisTemplate.delete(key + status);
    }

    public static List<AbstractMethod> getMethodList(final String key) {
        List<AbstractMethod> methodList = new ArrayList<>();
        final byte[] keyBytes = redisTemplateSerializable.getStringSerializer().serialize(key);
        long size = redisTemplateSerializable.opsForList().size(keyBytes);
        for (long i = 0; i <= size; i++) {
            AbstractMethod method = opsForList(key, AbstractMethod.class);
            if (method != null) {
                methodList.add(method);
            }
        }
        return methodList;
    }

}
