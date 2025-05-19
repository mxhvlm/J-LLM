package org.example.pipeline;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class JsonExtractorStep<T> implements IPipelineStep<Integer, Stream<T>> {
  private final Gson _gson;
  private final Class<T> _targetType;
  private final String _filePath;

  // Optional cursor and batch size
  private final Integer _cursor;
  private final Integer _batchSize;

  // Original constructor (full array)
  public JsonExtractorStep(Class<T> targetType, String filePath) {
    this(targetType, filePath, null, null);
  }

  // New constructor with cursor and batch size
  public JsonExtractorStep(Class<T> targetType, String filePath, Integer cursor, Integer batchSize) {
    _gson = new Gson();
    _targetType = targetType;
    _filePath = filePath;
    _cursor = cursor;
    _batchSize = batchSize;
  }

  @Override
  public Stream<T> process(Integer obj) {
    if (_filePath == null || !Files.exists(Paths.get(_filePath))) {
      throw new IllegalArgumentException("Invalid file path: " + _filePath);
    }

    try (FileReader reader = new FileReader(_filePath)) {
      JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();

      int start = _cursor != null ? _cursor : 0;
      int end = (_batchSize != null && _cursor != null) ? Math.min(start + _batchSize, array.size()) : array.size();

      return IntStream.range(start, end)
          .mapToObj(array::get)
          .map(jsonElement -> _gson.fromJson(jsonElement, _targetType));

    } catch (IOException e) {
      throw new RuntimeException("Failed to read or parse JSON file: " + _filePath, e);
    }
  }
}