package ru.yandex.javacourse.schedule.manager;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class HttpTaskServerFileBackedTest extends HttpTaskServerTest<FileBackedTaskManager> {
  public HttpTaskServerFileBackedTest() {
  }

  @BeforeEach
  public void setUp() throws IOException {
    File file = File.createTempFile("taskTmp", ".csv");
    super.taskManager = FileBackedTaskManager.loadFromFile(file);
    taskServer = new ru.yandex.javacourse.schedule.http.HttpTaskServer(taskManager);
    taskServer.start();
    client = HttpClient.newHttpClient();
    initTasks();
  }

  @AfterEach
  public void shutDown() {
    taskServer.stop();
  }
}