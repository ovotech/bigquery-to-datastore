package org.apache.beam.sdk.io.redis;

import org.apache.beam.repackaged.beam_sdks_java_io_redis.com.google.common.base.Preconditions;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import javax.annotation.Nullable;

public class MultiRedisWrite extends RedisIO.Write {
    private final RedisConnectionConfiguration connectionConfiguration;

    public MultiRedisWrite(RedisConnectionConfiguration connectionConfiguration) {
        this.connectionConfiguration = connectionConfiguration;
    }

    @Nullable
    @Override
    RedisConnectionConfiguration connectionConfiguration() {
        return connectionConfiguration;
    }

    @Override
    Builder builder() {
        return new Builder(this);
    }


    static final class Builder extends org.apache.beam.sdk.io.redis.RedisIO.Write.Builder {
        private RedisConnectionConfiguration connectionConfiguration;

        Builder() {
        }

        private Builder(RedisIO.Write source) {
            this.connectionConfiguration = source.connectionConfiguration();
        }

        org.apache.beam.sdk.io.redis.RedisIO.Write.Builder setConnectionConfiguration(@Nullable RedisConnectionConfiguration connectionConfiguration) {
            this.connectionConfiguration = connectionConfiguration;
            return this;
        }

        RedisIO.Write build() {
            return new MultiRedisWrite(this.connectionConfiguration);
        }
    }

    @Override
    public PDone expand(PCollection<KV<String, String>> input) {
        Preconditions.checkArgument(this.connectionConfiguration() != null, "withConnectionConfiguration() is required");
        input.apply(ParDo.of(new WriteFn(this)));
        return PDone.in(input.getPipeline());
    }

    private static class WriteFn extends DoFn<KV<String, String>, Void> {
        private static final int DEFAULT_BATCH_SIZE = 1000;
        private final RedisIO.Write spec;
        private transient Jedis jedis;
        private transient Pipeline pipeline;
        private int batchCount;

        WriteFn(RedisIO.Write spec) {
            this.spec = spec;
        }

        @Setup
        public void setup() {
            this.jedis = this.spec.connectionConfiguration().connect();
        }

        @StartBundle
        public void startBundle() {
            this.pipeline = this.jedis.pipelined();
            this.pipeline.multi();
            this.batchCount = 0;
        }

        @ProcessElement
        public void processElement(DoFn<KV<String, String>, Void>.ProcessContext processContext) {
            KV<String, String> record = (KV)processContext.element();
            this.pipeline.set(record.getKey(), record.getValue());
            ++this.batchCount;
            if (this.batchCount >= 1000) {
                this.pipeline.exec();
                this.batchCount = 0;
                this.pipeline.multi();
            }

        }

        @FinishBundle
        public void finishBundle() {
            this.pipeline.exec();
            this.batchCount = 0;
        }

        @Teardown
        public void teardown() {
            this.jedis.close();
        }
    }

}
