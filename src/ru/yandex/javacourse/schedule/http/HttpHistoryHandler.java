package ru.yandex.javacourse.schedule.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;

public class HttpHistoryHandler extends BaseHttpHandler implements HttpHandler {
  public HttpHistoryHandler(TaskManager taskManager) {
    super(taskManager);
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    if (exchange.getRequestMethod().equals("GET")) {
      List<Task> history = taskManager.getHistory();
      sendJson(exchange, history, 200);
    }
  }
}