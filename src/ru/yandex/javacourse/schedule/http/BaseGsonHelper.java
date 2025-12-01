package ru.yandex.javacourse.schedule.http;

import static java.util.Objects.nonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BaseGsonHelper {
  static class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy");

    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
      if (nonNull(localDateTime)) {
        jsonWriter.value(localDateTime.format(timeFormatter));
      } else {
        jsonWriter.nullValue();
      }
    }

    @Override
    public LocalDateTime read(JsonReader jsonReader) throws IOException {
      return LocalDateTime.parse(jsonReader.nextString(), timeFormatter);
    }
  }

  static class DurationTypeAdapter extends TypeAdapter<Duration> {

    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
      if (nonNull(duration)) {
        jsonWriter.value(duration.toMinutes());
      } else {
        jsonWriter.nullValue();
      }
    }

    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
      return Duration.ofMinutes(Long.parseLong(jsonReader.nextString()));
    }
  }

  private static Gson getGson() {
    return new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
        .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
        .setPrettyPrinting()
        .create();
  }

  public static <T> T fromJson(String json, Class<T> cls) {
    return getGson().fromJson(json, cls);
  }

  public static String toJson(Object obj) {
    return getGson().toJson(obj);
  }

  public static <T> List<T> listFromJson(String json, Class<T> cls) {
    TypeToken<?> typeToken = TypeToken.getParameterized(List.class, cls);
    return getGson().fromJson(json, typeToken.getType());
  }
}