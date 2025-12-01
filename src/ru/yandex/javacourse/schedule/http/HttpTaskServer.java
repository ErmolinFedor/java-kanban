package ru.yandex.javacourse.schedule.http;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;

public class HttpTaskServer {
  private static final int PORT = 8080;

  private final HttpServer httpServer;

  public HttpTaskServer(TaskManager taskManager) throws IOException {

    httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

    createContext(httpServer, taskManager);
  }

  public HttpTaskServer() throws IOException {
    httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

    TaskManager taskManager = Managers.getDefault();

    createContext(httpServer, taskManager);
  }

  private static void createContext(HttpServer httpServer, TaskManager taskManager) {
    httpServer.createContext("/tasks", new HttpTaskHandler(taskManager));
    httpServer.createContext("/subtasks", new HttpSubtaskHandler(taskManager));
    httpServer.createContext("/epics", new HttpEpicsHandler(taskManager));
    httpServer.createContext("/history", new HttpHistoryHandler(taskManager));
    httpServer.createContext("/prioritized", new HttpPrioritizedHandler(taskManager));
  }

  public void start() {
    httpServer.start();
    System.out.println("Server started at port: " + PORT);
  }

  public void stop() {
    httpServer.stop(0);
    System.out.println("Server stopped");
  }


  public static void main(String[] agrs) throws IOException {
    HttpTaskServer server = new HttpTaskServer();
    server.start();
  }
}