package org.apache.beam.sdk.io.redis;

import redis.clients.jedis.Jedis;

import javax.annotation.Nullable;

public class SslRedisConnectionConfiguration extends RedisConnectionConfiguration {
    private final String host;
    private final int port;
    private final String auth;
    private final int timeout;
    private final boolean ssl;

    public SslRedisConnectionConfiguration(String host, int port, String auth, int timeout, boolean ssl) {
        this.host = host;
        this.port = port;
        this.auth = auth;
        this.timeout = timeout;
        this.ssl = ssl;
    }

    String host() {
        return this.host;
    }

    int port() {
        return this.port;
    }

    @Nullable
    String auth() {
        return this.auth;
    }

    int timeout() {
        return this.timeout;
    }

    @Override
    public Jedis connect() {
        Jedis jedis = new Jedis(this.host(), this.port(), this.timeout, this.ssl);
        if (this.auth() != null) {
            jedis.auth(this.auth());
        }

        return jedis;
    }


    org.apache.beam.sdk.io.redis.RedisConnectionConfiguration.Builder builder() {
        return new Builder(this);
    }

    static final class Builder extends org.apache.beam.sdk.io.redis.RedisConnectionConfiguration.Builder {
        private String host;
        private Integer port;
        private String auth;
        private Integer timeout;
        private boolean ssl;

        Builder() {
        }

        private Builder(RedisConnectionConfiguration source) {
            this.host = source.host();
            this.port = source.port();
            this.auth = source.auth();
            this.timeout = source.timeout();
        }

        org.apache.beam.sdk.io.redis.RedisConnectionConfiguration.Builder setHost(String host) {
            if (host == null) {
                throw new NullPointerException("Null host");
            } else {
                this.host = host;
                return this;
            }
        }

        org.apache.beam.sdk.io.redis.RedisConnectionConfiguration.Builder setPort(int port) {
            this.port = port;
            return this;
        }

        org.apache.beam.sdk.io.redis.RedisConnectionConfiguration.Builder setAuth(@Nullable String auth) {
            this.auth = auth;
            return this;
        }

        org.apache.beam.sdk.io.redis.RedisConnectionConfiguration.Builder setTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        org.apache.beam.sdk.io.redis.RedisConnectionConfiguration.Builder setSsl(boolean ssl) {
            this.ssl = ssl;
            return this;
        }

        RedisConnectionConfiguration build() {
            String missing = "";
            if (this.host == null) {
                missing = missing + " host";
            }

            if (this.port == null) {
                missing = missing + " port";
            }

            if (this.timeout == null) {
                missing = missing + " timeout";
            }

            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            } else {
                return new SslRedisConnectionConfiguration(this.host, this.port, this.auth, this.timeout, this.ssl);
            }
        }
    }
}
