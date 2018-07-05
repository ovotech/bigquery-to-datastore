/**
 * Copyright (c) 2017 Yu Ishikawa.
 */
package com.github.yuiskw.beam;

import com.google.api.services.bigquery.model.TableReference;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.redis.MultiRedisWrite;
import org.apache.beam.sdk.io.redis.RedisConnectionConfiguration;
import org.apache.beam.sdk.io.redis.RedisIO;
import org.apache.beam.sdk.io.redis.SslRedisConnectionConfiguration;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.transforms.ParDo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This class is used for a Dataflow job which write parsed Laplace logs to BigQuery.
 */
public class BigQuery2Redis {

  /** command line options interface */
  public interface Options extends DataflowPipelineOptions {
    @Description("Input BigQuery dataset name")
    @Validation.Required
    String getInputBigQueryDataset();
    void setInputBigQueryDataset(String inputBigQueryDataset);

    @Description("Input BigQuery table name")
    @Validation.Required
    String getInputBigQueryTable();
    void setInputBigQueryTable(String inputBigQueryTable);

    @Description("Output Redis host")
    @Validation.Required
    String getOutputRedisHost();
    void setOutputRedisHost(String outputRedisHost);

    @Description("Output Redis port")
    @Validation.Required
    int getOutputRedisPort();
    void setOutputRedisPort(int outputRedisPort);

    @Description("Output Redis auth")
    String getOutputRedisAuth();
    void setOutputRedisAuth(String outputRedisAuth);

    @Description("Output Redis SSL")
    boolean isUseSslForRedis();
    void setUseSslForRedis(boolean useSslForRedis);


    @Description("BigQuery column for Redis key")
    @Validation.Required
    String getKeyColumn();
    void setKeyColumn(String keyColumn);

    @Description("Whether the output Redis value is a single field")
    boolean isSingleOutputValue();
    void setSingleOutputValue(boolean singleOutputValue);

  }

  public static void main(String[] args) {
    Options options = getOptions(args);

    String projectId = options.getProject();
    String datasetId = options.getInputBigQueryDataset();
    String tableId = options.getInputBigQueryTable();

    String redisHost = options.getOutputRedisHost();
    int redisPort = options.getOutputRedisPort();
    String redisAuth = options.getOutputRedisAuth();
    boolean isUseSslForRedis = options.isUseSslForRedis();
    String keyColumn = options.getKeyColumn();
    boolean isSingleOutputValue = options.isSingleOutputValue();

    // Input
    TableReference tableRef = new TableReference().setProjectId(projectId).setDatasetId(datasetId).setTableId(tableId);
    BigQueryIO.Read reader = BigQueryIO.read().from(tableRef);

    // Output
    RedisConnectionConfiguration connectionConfiguration =
            new SslRedisConnectionConfiguration(redisHost,
                    redisPort, redisAuth, 5000, isUseSslForRedis);

    RedisIO.Write redisWriter = new MultiRedisWrite(connectionConfiguration);

    // Build and run pipeline
    TableRow2RedisKVFn fn =
        new TableRow2RedisKVFn(keyColumn, isSingleOutputValue);
    Pipeline pipeline = Pipeline.create(options);
    pipeline
        .apply(reader)
        .apply(ParDo.of(fn))
        .apply(redisWriter);
    pipeline.run();
  }

  /**
   * Get command line options
   */
  public static Options getOptions(String[] args) {
    PipelineOptionsFactory.register(Options.class);
    Options options = PipelineOptionsFactory.fromArgs(args)
        .withValidation()
        .as(Options.class);
    return options;
  }

  /**
   * Get a parent path map
   *
   * e.g.) "Parent1:p1,Parent2:p2"
   */
  public static LinkedHashMap<String, String> parseParentPaths(String parentPaths) {
    LinkedHashMap<String, String> pathMap = new LinkedHashMap<String, String>();
    if (parentPaths != null) {
      // TODO validation
      for (String path : parentPaths.split(",")) {
        // trim
        String trimmed = path.replaceAll("(^\\s+|\\s+$)", "");

        // split with ":" and trim each element
        String[] elements = trimmed.split(":");
        String k = elements[0].replaceAll("(^\\s+|\\s+$)", "");
        String v = elements[1].replaceAll("(^\\s+|\\s+$)", "");
        pathMap.put(k, v);
      }
    }
    return pathMap;
  }

  /**
   * Get indexed column names
   *
   * @param indexedColumns a string separated by "," (i.e. "column1,column2,column3").
   * @return array of indexed column name.
   */
  public static List<String> parseIndexedColumns(String indexedColumns) {
    ArrayList<String> columns = new ArrayList<String>();
    if (indexedColumns != null) {
      for (String path : indexedColumns.split(",")) {
        // trim
        String column = path.replaceAll("(^\\s+|\\s+$)", "");
        columns.add(column);
      }
    }
    return columns;
  }
}
