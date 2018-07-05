package com.github.yuiskw.beam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.bigquery.model.TableRow;
import com.google.common.collect.Maps;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;

import java.util.*;


/**
 * This class is an Apache Beam function to convert TableRow to a KV of String to String.
 */
public class TableRow2RedisKVFn extends DoFn<TableRow, KV<String, String>> {


  /** BigQuery column for Redis key */
  private String keyColumn;
  /** Whether BigQuery input has only a single value column to be used for Redis value */
  private boolean isSingleValue;

  TableRow2RedisKVFn(String keyColumn, boolean isSingleValue) {
    this.keyColumn = keyColumn;
    this.isSingleValue = isSingleValue;
  }

  /**
   * Convert TableRow to Entity
   */
  @ProcessElement
  public void processElement(ProcessContext c) {
    try {
      TableRow row = c.element();
      KV<String, String> keyValue = convertTableRowToKeyValue(row);
      c.output(keyValue);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }


  /**
   * Convert TableRow to Key Value
   *
   * @param row TableRow of bigquery
   * @return converted Key Value
   * @throws JsonProcessingException if row could not be converted to JSON
   */
  private KV<String, String> convertTableRowToKeyValue(TableRow row) throws JsonProcessingException {
    String key = row.get(keyColumn).toString();

    Set<Map.Entry<String, Object>> entries = row.entrySet();
    Map<String, Object> data = Maps.newHashMap();
    for (Map.Entry<String, Object> entry : entries) {
      // Skip on the key column
      if (entry.getKey().equals(keyColumn)) {
        continue;
      }

      // Put a value in the builder
      String propertyName = entry.getKey();
      Object value = entry.getValue();
      data.put(propertyName, value);
    }

    String value;
    if (isSingleValue && data.size() == 1) {
      value = data.values().iterator().next().toString();
    } else {
      value = new ObjectMapper().writeValueAsString(data);
    }
    return KV.of(key, value);
  }

}
