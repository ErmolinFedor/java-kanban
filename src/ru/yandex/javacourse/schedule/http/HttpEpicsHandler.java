package ru.yandex.javacourse.schedule.http;

import static java.util.Objects.isNull;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import ru.yandex.javacourse.schedule.exeptions.OverlapTasksException;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.TaskType;

public class HttpEpicsHandler extends BaseHttpHandler implements HttpHandler {
  public HttpEpicsHandler(TaskManager taskManager) {
    super(taskManager);
  }
  @Override
  public void handle(HttpExchange exchange) throws IOException {

    try {
      switch (exchange.getRequestMethod()) {

        case "GET" -> {
          Optional<Integer> optId = getPathId(exchange);
          if (optId.isEmpty()) {
            sendJson(exchange, taskManager.getEpics(), 200);
          } else {
            Epic epic = taskManager.getEpic(optId.get());
            if (isNull(epic)) {
              sendNotFound(exchange);
            } else {
              if (exchange.getRequestURI().getPath().endsWith("/subtasks")) {
                List<Subtask> subtasks = taskManager.getEpicSubtasks(epic.getId());
                sendJson(exchange, subtasks, 200);
              } else {
                sendJson(exchange, epic, 200);
              }
            }

          }
        }

        case "POST" -> {
          try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Epic epic = BaseGsonHelper.fromJson(body, Epic.class);
            epic.setTaskType(TaskType.EPIC);
            if (isNull(taskManager.getTask(epic.getId()))) {
              int epicId = taskManager.addNewEpic(epic);
              sendJson(exchange, epicId, 201);
            } else {
              taskManager.updateEpic(epic);
              sendEmptyResponse(exchange, 201);
            }
          } catch (Exception e) {
            sendEmptyResponse(exchange, 501);
          }

        }

        case "DELETE" -> {
          Optional<Integer> optId = getPathId(exchange);
          if (optId.isPresent()) {
            int id = optId.get();
            taskManager.deleteEpic(id);
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