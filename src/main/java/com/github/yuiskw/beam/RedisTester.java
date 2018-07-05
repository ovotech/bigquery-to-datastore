/**
 * Copyright (c) 2017 Yu Ishikawa.
 */
package com.github.yuiskw.beam;

import org.apache.beam.sdk.io.redis.RedisConnectionConfiguration;
import org.apache.beam.sdk.io.redis.SslRedisConnectionConfiguration;
import redis.clients.jedis.Jedis;

/**
 * This class is used for a Dataflow job which write parsed Laplace logs to BigQuery.
 */
public class RedisTester {

  public static void main(String[] args) {

    RedisConnectionConfiguration connectionConfiguration =
            new SslRedisConnectionConfiguration("boost-customer-layer-redis.ovo-uat.aivencloud.com",
                    15662, "jw6pgsip1p4wm80y", 5000, true);

    Jedis jedis = connectionConfiguration.connect();
    String s = jedis.get("6075222");
    System.out.println(jedis.keys("*").size());
    //System.out.println(s);
  }

}
