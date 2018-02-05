package com.nowcoder.util;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

@Component
public class JedisAdapter implements InitializingBean{
    private static final Logger LOGGER = LoggerFactory.getLogger(JedisAdapter.class);
    private JedisPool pool = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        pool = new JedisPool();
    }

    private Jedis getJedis() {
        return pool.getResource();
    }

    public long sadd(String key,String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sadd(key,value);
        } catch (Exception e) {
            LOGGER.error("发生异常",e);
            return 0;
        } finally {
            if(jedis!=null) {
                jedis.close();
            }
        }
    }
    public long srem(String key,String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.srem(key,value);
        } catch (Exception e) {
            LOGGER.error("发生异常",e);
            return 0;
        } finally {
            if(jedis!=null) {
                jedis.close();
            }
        }
    }

    public boolean isMember(String key,String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.sismember(key,value);
        } catch (Exception e) {
            LOGGER.error("发生异常",e);
            return false;
        } finally {
            if(jedis!=null) {
                jedis.close();
            }
        }
    }

    public long scad(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.scard(key);
        } catch (Exception e) {
            LOGGER.error("发生异常",e);
            return 0;
        } finally {
            if(jedis!=null) {
                jedis.close();
            }
        }
    }

    public String get(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return getJedis().get(key);
        } catch (Exception e) {
            LOGGER.error("发生异常" + e.getMessage());
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void set(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.set(key, value);
        } catch (Exception e) {
            LOGGER.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void setObject(String key, Object obj) {
        set(key, JSON.toJSONString(obj));
    }

    public <T> T getObject(String key) {
        String value = get(key);
        if(value != null) {
            return (T)JSON.parseObject(value);
        }
        return null;
    }


    public long lpush(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lpush(key, value);
        } catch (Exception e) {
            LOGGER.error("发生异常" + e.getMessage());
            return 0;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public List<String> brpop(int timeout, String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.brpop(timeout, key);
        } catch (Exception e) {
            LOGGER.error("发生异常" + e.getMessage());
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

}
