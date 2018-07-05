/**
 * Copyright (c) 2017 Yu Ishikawa.
 */
package com.github.yuiskw.beam.redis;

import com.google.api.services.bigquery.model.TableRow;
import com.google.datastore.v1.*;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.*;


/**
 * This class is an Apache Beam function to convert TableRow to Entity.
 */
public class TableRow2RedisKVFn extends DoFn<TableRow, KV<String, String>> {


  /** BigQuery column for Redis key */
  private String keyColumn;
  /** BigQuery column for Redis value */
  private String valueColumn;

  public TableRow2RedisKVFn(String keyColumn, String valueColumn) {
    this.keyColumn = keyColumn;
    this.valueColumn = valueColumn;
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
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }


  /**
   * Convert TableRow to Key Value
   *
   * @param row TableRow of bigquery
   * @return converted Key Value
   * @throws ParseException
   */
  private KV<String, String> convertTableRowToKeyValue(TableRow row) throws ParseException {
    String key = row.get(keyColumn).toString();
    String value = row.get(valueColumn).toString();
    return KV.of(key, value);
  }

}
