package ru.yandex.javacourse.schedule.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.IN_PROGRESS;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.http.BaseGsonHelper;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;

public abstract class HttpTaskServerTest<T extends TaskManager> {
  protected final LocalDateTime START_TIME = LocalDateTime.of(2026, 1, 1, 0, 0);
  protected TaskManager taskManager;
  protected ru.yandex.javacourse.schedule.http.HttpTaskServer taskServer;
  protected HttpClient client;
  protected final int EPIC_ID = 2;
  protected Task task1;
  protected Epic epic2;
  protected Subtask subtask3;
  protected Subtask subtask4;
  protected List<Task> tasks;
  protected List<Epic> epics;
  protected List<Subtask> subtasks;

  private static final String ERROR_MSG =
      "Время выполнения задачи пересекается со временем уже существующей задачи. "
          + "Выберите другую дату.";

  protected void initTasks() {
    tasks = new ArrayList<>();
    epics = new ArrayList<>();
    subtasks = new ArrayList<>();
    task1 = new Task(1, "Test 1", "Testing task 1", NEW, START_TIME, 15);
    taskManager.addNewTask(task1);
    epic2 = new Epic(2, "Epic 1", "Testing epic 1",
        NEW, START_TIME.plusMinutes(15), 30);
    taskManager.addNewEpic(epic2);
    subtask3 = new Subtask(3, "SubTask 1", "Testing SubTask 1",
        NEW, EPIC_ID, START_TIME.plusMinutes(15), 15);
    taskManager.addNewSubtask(subtask3);
    subtask4 = new Subtask(4, "SubTask 2", "Testing SubTask 2",
        NEW, EPIC_ID, START_TIME.plusMinutes(30), 15);
    taskManager.addNewSubtask(subtask4);
    tasks.add(task1);
    epics.add(epic2);
    subtasks.add(subtask3);
    subtasks.add(subtask4);
    taskManager.getTask(task1.getId());
    taskManager.getEpic(epic2.getId());
    taskManager.getSubtask(subtask3.getId());
    taskManager.getSubtask(subtask4.getId());
  }

  public HttpTaskServerTest() {
  }

  private URI getUri(String path) {
    return URI.create("http://localhost:8080/" + path);
  }

  private HttpRequest postHttpRequest(URI url, String body) {
    return HttpRequest.newBuilder().uri(url)
        .POST(HttpRequest.BodyPublishers.ofString(body)).build();
  }

  private HttpRequest getHttpRequest(URI url) {
    return HttpRequest.newBuilder().uri(url).GET().build();
  }

  private HttpRequest deleteHttpRequest(URI url) {
    return HttpRequest.newBuilder().uri(url).DELETE().build();
  }

  private void assertTasks(List<Task> expected, List<Task> actual) {
    assertNotNull(actual, "Tasks are empty");
    assertEquals(expected.size(), actual.size(), "Tasks size not equals");
    assertEquals(expected, actual, "Tasks does not equals");
  }

  private void assertEpics(List<Epic> expected, List<Epic> actual) {
    assertNotNull(actual, "Epics are empty");
    assertEquals(expected.size(), actual.size(), "Epics size not equals");
    assertEquals(expected, actual, "Epics does not equals");
  }

  private void assertSubtasks(List<Subtask> expected, List<Subtask> actual) {
    assertNotNull(actual, "Subtasks are empty");
    assertEquals(expected.size(), actual.size(), "Subtasks size not equals");
    assertEquals(expected, actual, "Subtasks does not equals");
  }

  @Test
  public void testAddTask() throws IOException, InterruptedException {
    Task task2 = new Task(5, "Test 1", "Testing task 1",
        NEW, START_TIME.plusMinutes(45), 15);
    tasks.add(task2);
    String taskJson = BaseGsonHelper.toJson(task2);
    URI url = getUri("tasks");
    HttpRequest request = postHttpRequest(url, taskJson);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(201, response.statusCode());

    List<Task> actual = taskManager.getTasks();
    assertTasks(tasks, actual);
  }

  @Test
  public void testUpdateTask() throws IOException, InterruptedException {
    task1.setStatus(IN_PROGRESS);
    String taskJson = BaseGsonHelper.toJson(task1);
    URI url = getUri("tasks");
    HttpRequest request = postHttpRequest(url, taskJson);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(201, response.statusCode());

    List<Task> actual = taskManager.getTasks();
    assertTasks(tasks, actual);
  }

  @Test
  public void testGetTasks() throws IOException, InterruptedException {
    URI url = getUri("tasks");
    HttpRequest request = getHttpRequest(url);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode());

    List<Task> actual = BaseGsonHelper.listFromJson(response.body(), Task.class);
    assertTasks(tasks, actual);
  }

  @Test
  public void testGetTaskById() throws IOException, InterruptedException {
    URI url = getUri("tasks/1");
    HttpRequest request = getHttpRequest(url);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode());

    Task actual = BaseGsonHelper.fromJson(response.body(), Task.class);
    assertEquals(tasks.getFirst(), actual, "Task should be equals");
  }

  @Test
  public void testDeleteTask() throws IOException, InterruptedException {
    Task task = new Task(5,"Test 2", "Testing task 2",
        NEW, START_TIME.plusMinutes(45), 15);
    taskManager.addNewTask(task);
    tasks.add(task);

    URI url = getUri("tasks/1");
    HttpRequest request = deleteHttpRequest(url);
    tasks.remove(tasks.getFirst());

    HttpResponse<String> response2 = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response2.statusCode());
    assertTasks(tasks, taskManager.getTasks());
  }

  @Test
  public void testFailureGetTaskById() throws IOException, InterruptedException {
    URI url = getUri("tasks/2");
    HttpRequest request = getHttpRequest(url);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(404, response.statusCode());
  }

  @Test
  public void testFailureAddTask() throws IOException, InterruptedException {
    Task task = new Task(2,"Test 2", "Testing task 2",
        NEW, START_TIME, 15);
    String taskJson = BaseGsonHelper.toJson(task);
    URI url = getUri("tasks");
    HttpRequest request = postHttpRequest(url, taskJson);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(406, response.statusCode());
    assertEquals(ERROR_MSG, response.body(), "Error message does not equals");
  }

  @Test
  public void testAddEpic() throws IOException, InterruptedException {
    Epic newEpic = new Epic(5,"Epic 5", "Testing epic 5",
        NEW, START_TIME.plusMinutes(45), 15);
    epics.add(newEpic);
    String epicJson = BaseGsonHelper.toJson(newEpic);
    URI url = getUri("epics");
    HttpRequest request = postHttpRequest(url, epicJson);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(201, response.statusCode());

    List<Epic> actual = taskManager.getEpics();
    assertEpics(epics, actual);
  }

  @Test
  public void testGetEpics() throws IOException, InterruptedException {
    URI url = getUri("epics");
    HttpRequest request = getHttpRequest(url);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode());

    List<Epic> actual = BaseGsonHelper.listFromJson(response.body(), Epic.class);
    assertEpics(epics, actual);
  }

  @Test
  public void testGetEpicById() throws IOException, InterruptedException {
    URI url = getUri("epics/" + epic2.getId());
    HttpRequest request = getHttpRequest(url);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode());

    Epic actual = BaseGsonHelper.fromJson(response.body(), Epic.class);
    assertEquals(epic2, actual, "Epics should be equals");
  }

  @Test
  public void testDeleteEpic() throws IOException, InterruptedException {
    Epic epic = new Epic(5,"Epic 5", "Testing epic 5",
        NEW, START_TIME.plusMinutes(45), 15);
    taskManager.addNewEpic(epic);
    epics.add(epic);

    URI url = getUri("epics/" + epic2.getId());
    HttpRequest request = deleteHttpRequest(url);
    epics.remove(epic2);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode());
    assertEpics(epics, taskManager.getEpics());
  }

  @Test
  public void testFailureGetEpicById() throws IOException, InterruptedException {
    URI url = getUri("epics/99");
    HttpRequest request = getHttpRequest(url);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(404, response.statusCode());
  }

  @Test
  public void testGetSubtaskByEpic() throws IOException, InterruptedException {
    URI url = getUri(String.format("epics/%d/subtasks", epic2.getId()));
    HttpRequest request = getHttpRequest(url);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode());
    List<Subtask> actual = taskManager.getEpicSubtasks(epic2.getId());
    assertSubtasks(subtasks, actual);
  }

  @Test
  public void testAddSubtask() throws IOException, InterruptedException {
    Subtask subtask5 = new Subtask(5, "SubTask 3", "Testing SubTask 3",
        NEW, EPIC_ID, START_TIME.plusMinutes(45), 15);
    subtasks.add(subtask5);
    String taskJson = BaseGsonHelper.toJson(subtask5);
    URI url = getUri("subtasks");
    HttpRequest request = postHttpRequest(url, taskJson);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(201, response.statusCode());

    List<Subtask> actual = taskManager.getSubtasks();
    assertSubtasks(subtasks, actual);
  }

  @Test
  public void testUpdateSubtask() throws IOException, InterruptedException {
    subtask4.setStatus(IN_PROGRESS);
    String taskJson = BaseGsonHelper.toJson(subtask4);
    URI url = getUri("subtasks");
    HttpRequest request = postHttpRequest(url, taskJson);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(201, response.statusCode());

    List<Subtask> actual = taskManager.getSubtasks();
    assertSubtasks(subtasks, actual);
  }

  @Test
  public void testGetSubtasks() throws IOException, InterruptedException {
    URI url = getUri("subtasks");
    HttpRequest request = getHttpRequest(url);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode());

    List<Subtask> actual = BaseGsonHelper.listFromJson(response.body(), Subtask.class);
    assertSubtasks(subtasks, actual);
  }

  @Test
  public void testGetSubtaskById() throws IOException, InterruptedException {
    URI url = getUri("subtasks/" + subtask3.getId());
    HttpRequest request = getHttpRequest(url);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode());

    Subtask actual = BaseGsonHelper.fromJson(response.body(), Subtask.class);
    assertEquals(subtask3, actual, "Subtasks should be equals");
  }

  @Test
  public void testDeleteSubtask() throws IOException, InterruptedException {
    URI url = getUri("subtasks/" + subtask3.getId());
    HttpRequest request2 = deleteHttpRequest(url);
    subtasks.remove(subtask3);

    HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response2.statusCode());
    assertSubtasks(subtasks, taskManager.getSubtasks());
  }

  @Test
  public void testFailureGetSubtaskById() throws IOException, InterruptedException {
    URI url = getUri("subtasks/99");
    HttpRequest request = getHttpRequest(url);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(404, response.statusCode());
  }

  @Test
  public void testFailureAddSubtask() throws IOException, InterruptedException {
    Subtask subtask5 = new Subtask(5, "SubTask 5", "Testing SubTask 5",
        NEW, EPIC_ID, START_TIME.plusMinutes(20), 15);

    String taskJson = BaseGsonHelper.toJson(subtask5);
    URI url = getUri("subtasks");
    HttpRequest request = postHttpRequest(url, taskJson);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(406, response.statusCode());
    assertEquals(ERROR_MSG, response.body(), "Error message does not equals");
  }

  @Test
  public void testGetHistory() throws IOException, InterruptedException {
    URI url = getUri("history");
    HttpRequest request = getHttpRequest(url);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode());

    List<Task> actual = BaseGsonHelper.listFromJson(response.body(), Task.class);

    assertNotNull(taskManager.getHistory(), "Tasks are empty");
    assertEquals(taskManager.getHistory().size(), actual.size(), "Tasks size not equals");
  }

  @Test
  public void testPrioritizedTasks() throws IOException, InterruptedException {
    URI url = getUri("prioritized");
    HttpRequest request = getHttpRequest(url);

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode());

    List<Task> actual = BaseGsonHelper.listFromJson(response.body(), Task.class);

    assertNotNull(actual, "Tasks are empty");
    assertEquals(taskManager.getPrioritizedTasks().size(), actual.size(), "Tasks size not equals");
  }
}