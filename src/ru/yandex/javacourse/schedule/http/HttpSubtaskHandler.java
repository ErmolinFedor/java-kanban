package ru.yandex.javacourse.schedule.http;

import static java.util.Objects.isNull;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import ru.yandex.javacourse.schedule.exeptions.OverlapTasksException;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.TaskType;

public class HttpSubtaskHandler extends BaseHttpHandler implements HttpHandler {
  public HttpSubtaskHandler(TaskManager taskManager) {
    super(taskManager);
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {

    try {
      switch (exchange.getRequestMethod()) {

        case "GET" -> {
          Optional<Integer> optId = getPathId(exchange);
          if (optId.isEmpty()) {
            sendJson(exchange, taskManager.getSubtasks(), 200);
          } else {
            Subtask subtask = taskManager.getSubtask(optId.get());
            if (isNull(subtask)) {
              sendNotFound(exchange);
            } else {
              sendJson(exchange, subtask, 200);
            }

          }
        }

        case "POST" -> {
          String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
          Subtask subtask = BaseGsonHelper.fromJson(body, Subtask.class);
          subtask.setTaskType(TaskType.SUBTASK);
          if (isNull(taskManager.getSubtask(subtask.getId()))) {
            int subtaskId = taskManager.addNewSubtask(subtask);
            sendJson(exchange, subtaskId, 201);
          } else {
            taskManager.updateSubtask(subtask);
            sendEmptyResponse(exchange, 201);
          }
        }

        case "DELETE" -> {
          Optional<Integer> optId = getPathId(exchange);
          if (optId.isPresent()) {
            int id = optId.get();
            taskManager.deleteSubtask(id);
            sendEmptyResponse(exchange, 200);
          }
        }
      }
    } catch (OverlapTasksException e) {
      sendHasInteractions(exchange, e.getMessage());
    } catch (Throwable e) {
      sendInternal(exchange);
    }
  }
}