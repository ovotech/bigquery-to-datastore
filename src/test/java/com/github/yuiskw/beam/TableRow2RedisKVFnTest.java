package com.github.yuiskw.beam;

import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;

import java.util.Date;

import static org.junit.Assert.*;

public class TableRow2RedisKVFnTest {

    private TableRow getMultiTableRow() {
        TableRow row = new TableRow();
        String timestamp = Instant.now().toString();
        Instant.parse(timestamp);
        row.set("my-key", 123)
                .set("long_value", 1L)
                .set("name", "abc")
                .set("bool_value", true)
                .set("float_value", 1.23)
                .set("child", new TableRow().set("hoge", "fuga"));
        return row;
    }


    private TableRow getSingleTableRow() {
        TableRow row = new TableRow();
        String timestamp = Instant.now().toString();
        Instant.parse(timestamp);
        row.set("my-key", 123)
           .set("my-value", "hello");
        return row;
    }

    @Test
    public void shouldConvertSingleValue() {
        DoFn.ProcessContext mockContext = mock(DoFn.ProcessContext.class);
        when(mockContext.element()).thenReturn(getSingleTableRow());

        TableRow2RedisKVFn underTest = new TableRow2RedisKVFn("my-key", true);
        underTest.processElement(mockContext);

        ArgumentCaptor<KV<String, String>> captor = ArgumentCaptor.forClass(KV.class);
        verify(mockContext).output(captor.capture());
        KV<String, String> output = captor.getValue();

        assertEquals(output.getKey(), "123");
        assertEquals(output.getValue(), "hello");
    }


    @Test
    public void shouldConvertSingleValueIntoObject() {
        DoFn.ProcessContext mockContext = mock(DoFn.ProcessContext.class);
        when(mockContext.element()).thenReturn(getSingleTableRow());

        TableRow2RedisKVFn underTest = new TableRow2RedisKVFn("my-key", false);
        underTest.processElement(mockContext);

        ArgumentCaptor<KV<String, String>> captor = ArgumentCaptor.forClass(KV.class);
        verify(mockContext).output(captor.capture());
        KV<String, String> output = captor.getValue();

        assertEquals(output.getKey(), "123");
        assertEquals(output.getValue(), "{\"my-value\":\"hello\"}");
    }

    @Test
    public void shouldConvertMultiValueIntoObject() {
        DoFn.ProcessContext mockContext = mock(DoFn.ProcessContext.class);
        when(mockContext.element()).thenReturn(getMultiTableRow());

        TableRow2RedisKVFn underTest = new TableRow2RedisKVFn("my-key", false);
        underTest.processElement(mockContext);

        ArgumentCaptor<KV<String, String>> captor = ArgumentCaptor.forClass(KV.class);
        verify(mockContext).output(captor.capture());
        KV<String, String> output = captor.getValue();

        assertEquals(output.getKey(), "123");
        assertEquals(output.getValue(), "{\"name\":\"abc\",\"bool_value\":true,\"float_value\":1.23,\"long_value\":1,\"child\":{\"hoge\":\"fuga\"}}");
    }

    @Test
    public void shouldConvertMultiValueIntoObjectEvenIfSingleFlagSpecified() {
        DoFn.ProcessContext mockContext = mock(DoFn.ProcessContext.class);
        when(mockContext.element()).thenReturn(getMultiTableRow());

        TableRow2RedisKVFn underTest = new TableRow2RedisKVFn("my-key", true);
        underTest.processElement(mockContext);

        ArgumentCaptor<KV<String, String>> captor = ArgumentCaptor.forClass(KV.class);
        verify(mockContext).output(captor.capture());
        KV<String, String> output = captor.getValue();

        assertEquals(output.getKey(), "123");
        assertEquals(output.getValue(), "{\"name\":\"abc\",\"bool_value\":true,\"float_value\":1.23,\"long_value\":1,\"child\":{\"hoge\":\"fuga\"}}");
    }
}