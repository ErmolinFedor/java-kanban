package ru.yandex.javacourse.schedule.http;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ru.yandex.javacourse.schedule.manager.TaskManager;

public class BaseHttpHandler {
  private static final Pattern pathPattern = Pattern.compile("/(\\d+)");
  protected final TaskManager taskManager;

  public BaseHttpHandler(TaskManager taskManager) {
    this.taskManager = taskManager;
  }

  void sendText(HttpExchange exchange, String text, int responseCode) throws IOException {
    byte[] resp = text.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
    exchange.sendResponseHeaders(responseCode, resp.length);
    exchange.getResponseBody().write(resp);
    exchange.close();
  }

  void sendNotFound(HttpExchange exchange) throws IOException {
    sendEmptyResponse(exchange, 404);
  }

  void sendInternal(HttpExchange exchange) throws IOException {
    sendText(exchange,"Internal Server Error", 500);
  }

  void sendHasInteractions(HttpExchange exchange, String message) throws IOException {
    sendText(exchange, message, 406);
  }

  void sendJson(HttpExchange exchange, Object responseObject, int responseCode) throws IOException {
    String responseString = BaseGsonHelper.toJson(responseObject);
    sendText(exchange, responseString, responseCode);
  }

  void sendEmptyResponse(HttpExchange exchange, int responseCode) throws IOException {
    exchange.sendResponseHeaders(responseCode, 0);
    exchange.close();
  }

  Optional<Integer> getPathId(HttpExchange exchange) {
    String path = exchange.getRequestURI().getPath();
    Matcher match = pathPattern.matcher(path);
    if (match.find()) {
      return Optional.of(Integer.parseInt(match.group(1)));
    }
    return Optional.empty();
  }
}