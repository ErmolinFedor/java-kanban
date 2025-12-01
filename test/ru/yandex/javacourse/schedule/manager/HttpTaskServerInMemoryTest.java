package ru.yandex.javacourse.schedule.manager;

import java.io.IOException;
import java.net.http.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class HttpTaskServerInMemoryTest extends HttpTaskServerTest<InMemoryTaskManager> {

  public HttpTaskServerInMemoryTest() {
  }

  @BeforeEach
  public void setUp() throws IOException {
    super.taskManager = new InMemoryTaskManager();
    super.taskServer = new ru.yandex.javacourse.schedule.http.HttpTaskServer(taskManager);
    super.taskServer.start();
    super.client = HttpClient.newHttpClient();
    initTasks();
  }

  @AfterEach
  public void shutDown() {
    taskServer.stop();
  }
}