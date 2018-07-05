package com.github.yuiskw.beam;

import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.DoFn;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class TableRow2RedisKVFnTest {

    private TableRow getTestTableRow() {
        TableRow row = new TableRow();
        String timestamp = Instant.now().toString();
        Instant.parse(timestamp);
        row.set("uuid", "")
                .set("user_id", 123)
                .set("long_value", 1L)
                .set("name", "abc")
                .set("bool_value", true)
                .set("float_value", 1.23)
                .set("date", new Date())
                .set("datetime", new DateTime(new Date()))
                .set("ts", timestamp)
                .set("child", new TableRow().set("hoge", "fuga"));
        return row;
    }


    private TableRow getSingleTestTableRow() {
        TableRow row = new TableRow();
        String timestamp = Instant.now().toString();
        Instant.parse(timestamp);
        row.set("my-key", 123)
                .set("my-value", "hello")
        return row;
    }

    @Test
    public void shouldConvertSingleValue() {
        DoFn.ProcessContext mockContext = Mockito.mock(DoFn.ProcessContext.class);
        Mockito.when(mockContext.element()).thenReturn(getSingleTestTableRow());

        TableRow2RedisKVFn underTest = new TableRow2RedisKVFn("my-key", true);
        underTest.
    }
}