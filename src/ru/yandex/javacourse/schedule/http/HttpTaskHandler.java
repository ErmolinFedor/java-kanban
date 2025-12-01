package ru.yandex.javacourse.schedule.http;

import static java.util.Objects.isNull;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import ru.yandex.javacourse.schedule.exeptions.OverlapTasksException;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskType;

public class HttpTaskHandler extends BaseHttpHandler implements HttpHandler {
  public HttpTaskHandler(TaskManager taskManager) {
    super(taskManager);
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {

    try {
      switch (exchange.getRequestMethod()) {

        case "GET" -> {
          Optional<Integer> optId = getPathId(exchange);
          if (optId.isEmpty()) {
            sendJson(exchange, taskManager.getTasks(), 200);
          } else {
            Task task = taskManager.getTask(optId.get());
            if (isNull(task)) {
              sendNotFound(exchange);
            } else {
              sendJson(exchange, task, 200);
            }

          }
        }

        case "POST" -> {
          String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
          Task task = BaseGsonHelper.fromJson(body, Task.class);
          task.setTaskType(TaskType.TASK);
          if (isNull(taskManager.getTask(task.getId()))) {
            int taskId = taskManager.addNewTask(task);
            sendJson(exchange, taskId, 201);
          } else {
            taskManager.updateTask(task);
            sendEmptyResponse(exchange, 201);
          }
        }

        case "DELETE" -> {
          Optional<Integer> optId = getPathId(exchange);
          if (optId.isPresent()) {
            int id = optId.get();
            taskManager.deleteTask(id);
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