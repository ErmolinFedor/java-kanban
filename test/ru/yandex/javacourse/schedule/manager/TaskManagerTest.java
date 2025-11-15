package ru.yandex.javacourse.schedule.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

abstract class TaskManagerTest<T extends TaskManager> {
  protected T taskManager;
  protected final LocalDateTime START_TIME = LocalDateTime.of(2026, 1, 1, 0, 0);
  protected final int EPIC_ID = 2;
  protected Task task1;
  protected Epic epic2;
  protected Subtask subtask3;
  protected Subtask subtask4;
  protected  List<Task> tasks;
  protected List<Epic> epics;
  protected List<Subtask> subtasks;

  protected void initTasks() {
    tasks = new ArrayList<>();
    epics = new ArrayList<>();
    subtasks = new ArrayList<>();
    task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, START_TIME, 15);
    taskManager.addNewTask(task1);
    epic2 = new Epic(2, "Epic 1", "Testing epic 1");
    taskManager.addNewEpic(epic2);
    subtask3 = new Subtask(3, "SubTask 1", "Testing SubTask 1", TaskStatus.NEW, EPIC_ID, START_TIME.plusMinutes(15), 15);
    taskManager.addNewSubtask(subtask3);
    subtask4 = new Subtask(4, "SubTask 2", "Testing SubTask 2", TaskStatus.NEW, EPIC_ID, START_TIME.plusMinutes(30), 15);
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

  @Test
  void getTasks() {
    List<Task> actual = taskManager.getTasks();
    assertEquals(tasks.size(), actual.size(), "Size should be equals");
    for (Task task : actual) {
      assertEquals(task1, task, "Task should be equals");
    }
  }

  @Test
  void getSubtasks() {
    List<Subtask> actual = taskManager.getSubtasks();
    assertEquals(subtasks.size(), actual.size(), "Size should be equals");
    assertEquals(subtasks, actual, "Subtask should be equals");
  }

  @Test
  void getEpics() {
    List<Epic> actual = taskManager.getEpics();
    assertEquals(epics.size(), actual.size(), "Size should be equals");
    for (Epic epic : actual) {
      assertEquals(epic2, epic, "Epic should be equals");
    }
  }

  @Test
  void getEpicSubtasks() {
    List<Subtask> actual = taskManager.getEpicSubtasks(EPIC_ID);
    assertEquals(subtasks, actual, "Subtask should be equals");
  }

  @Test
  void getTaskById() {
    Task actual = taskManager.getTask(1);
    assertEquals(task1, actual, "Task should be equals");
  }

  @Test
  void getSubtaskById() {
    Subtask actual = taskManager.getSubtask(3);
    assertEquals(subtask3, actual, "Subtask should be equals");
  }

  @Test
  void getEpicById() {
    Epic actual = taskManager.getEpic(2);
    assertEquals(epic2, actual, "Epic should be equals");
  }

  @ParameterizedTest()
  @CsvSource({"NEW, 2026-01-01T00:45, 1", "IN_PROGRESS, 2026-01-01T00:45, 15",
      "DONE, 2026-01-01T00:45, 15"})
  void addNewTask(String status, String startTime, String duration) {
    Task task2 = new Task(5, "Test 2", "Testing task 2",
        TaskStatus.valueOf(status), LocalDateTime.parse(startTime), Long.parseLong(duration));
    taskManager.addNewTask(task2);
    tasks.add(task2);
    List<Task> actualTasks = taskManager.getTasks();
    assertEquals(tasks.size(), actualTasks.size(), "Size should be equals");
    for (Task task : tasks) {
      Task actual = actualTasks.stream()
          .filter(t -> t.getId() == task.getId())
          .findFirst()
          .orElseThrow();
      assertEquals(task, actual, "Task should be equals");
    }
  }

  @Test
  void addNewEpic() {
    Epic epic3 = new Epic(5, "Epic 2", "Testing epic2");
    epics.add(epic3);
    taskManager.addNewEpic(epic3);
    List<Epic> actualEpics = taskManager.getEpics();
    assertEquals(epics.size(), actualEpics.size(), "Size should be equals");
    for (Epic epic: epics) {
      Epic actual = actualEpics.stream()
          .filter(e -> e.getId() == epic.getId())
          .findFirst()
          .orElseThrow();
      assertEquals(epic, actual, "Epic should be equals");
    }
  }

  @ParameterizedTest()
  @CsvSource({"NEW, 2026-01-01T00:45, 1", "IN_PROGRESS, 2026-01-01T00:45, 15",
      "DONE, 2026-01-01T00:45, 15"})
  void addNewSubtask(String status, String startTime, String duration) {
    Subtask subtask5 =new Subtask(5, "SubTask 3", "Testing SubTask 3",
        TaskStatus.valueOf(status), EPIC_ID, LocalDateTime.parse(startTime),
        Long.parseLong(duration));
    taskManager.addNewSubtask(subtask5);
    subtasks.add(subtask5);
    List<Subtask> actual = taskManager.getEpicSubtasks(EPIC_ID);
    assertEquals(subtasks.size(), actual.size(), "Size should be equals");
    assertEquals(subtasks, actual, "Subtask should be equals");
  }

  @ParameterizedTest()
  @CsvSource({"NEW, 2026-01-01T00:45, 1", "IN_PROGRESS, 2026-01-01T00:45, 15",
      "DONE, 2026-01-01T00:45, 15"})
  void updateTask(String status, String startTime, String duration) {
    task1.setStatus(TaskStatus.valueOf(status));
    task1.setStartTime(LocalDateTime.parse(startTime));
    task1.setDuration(Long.parseLong(duration));
    taskManager.updateTask(task1);
    List<Task> actualTasks = taskManager.getTasks();
    assertEquals(tasks.size(), actualTasks.size(), "Size should be equals");
    for (Task task : tasks) {
      Task actual = actualTasks.stream()
          .filter(t -> t.getId() == task.getId())
          .findFirst()
          .orElseThrow();
      assertEquals(task, actual, "Task should be equals");
    }
  }

  @ParameterizedTest()
  @CsvSource({"NEW", "IN_PROGRESS", "DONE"})
  void updateEpic(String status) {
    epic2.setStatus(TaskStatus.valueOf(status));
    taskManager.updateEpic(epic2);
    List<Epic> actualEpics = taskManager.getEpics();
    assertEquals(epics.size(), actualEpics.size(), "Size should be equals");
    assertEquals(epics, actualEpics, "Epic should be equals");
  }

  @ParameterizedTest()
  @CsvSource({"NEW, 2026-01-01T00:45, 1", "IN_PROGRESS, 2026-01-01T00:45, 15",
      "DONE, 2026-01-01T00:45, 15"})
  void updateSubtask(String status, String startTime, String duration) {
    subtask3.setStatus(TaskStatus.valueOf(status));
    subtask3.setStartTime(LocalDateTime.parse(startTime));
    subtask3.setDuration(Long.parseLong(duration));
    taskManager.updateSubtask(subtask3);
    List<Subtask> actual = taskManager.getEpicSubtasks(EPIC_ID);
    assertEquals(subtasks, actual, "Subtasks should be equals");
  }

  @Test
  void deleteTask() {
    Task task2 = new Task(5, "Test 5", "Testing task 5",
        TaskStatus.NEW, START_TIME.plusMinutes(45), 15);
    taskManager.addNewTask(task2);
    tasks.add(task2);
    List<Task> actualTasks = taskManager.getTasks();
    assertEquals(tasks.size(), actualTasks.size(), "Size should be equals");
    assertEquals(tasks, taskManager.getTasks(), "Task should be equals");

    taskManager.deleteTask(task1.getId());
    tasks.remove(task1);
    assertEquals(tasks.size(), taskManager.getTasks().size(), "Size should be equals");
    assertEquals(tasks, taskManager.getTasks(), "Tasks should be equals");
  }

  @Test
  void deleteEpic() {
    Epic epic3 = new Epic(5, "Epic 2", "Testing epic2");
    epics.add(epic3);
    taskManager.addNewEpic(epic3);
    assertEquals(epics.size(), taskManager.getEpics().size(), "Size should be equals");
    assertEquals(epics, taskManager.getEpics(), "Epics should be equals");
      taskManager.deleteEpic(epic3.getId());
      epics.remove(epic3);

      List<Epic> actualEpics = taskManager.getEpics();
      assertEquals(epics.size(), actualEpics.size(), "Size should be equals");
      assertEquals(epics, actualEpics, "Epics should be equals");
  }

  @Test
  void deleteSubtask() {
    Subtask subtask5 =new Subtask(5, "SubTask 3", "Testing SubTask 3",
        NEW, EPIC_ID, START_TIME.plusMinutes(45), 15);
    taskManager.addNewSubtask(subtask5);
    subtasks.add(subtask5);
    assertEquals(subtasks, taskManager.getEpicSubtasks(EPIC_ID), "Subtask should be equals");
    subtasks.remove(subtask5);
    taskManager.deleteSubtask(subtask5.getId());
    List<Subtask> actual = taskManager.getEpicSubtasks(EPIC_ID);
    assertEquals(subtasks, actual, "Subtask should be equals");
  }

  @Test
  void deleteTasks() {
    taskManager.deleteTasks();
    List<Task> actualTasks = taskManager.getTasks();
    assertEquals(0, actualTasks.size(), "Size should be equals");
  }

  @Test
  void deleteSubtasks() {
    taskManager.deleteSubtasks();
    List<Subtask> actualSubtasks = taskManager.getSubtasks();
    assertEquals(0, actualSubtasks.size(), "Size should be equals");
  }

  @Test
  void deleteEpics() {
    taskManager.deleteEpics();
    List<Epic> actualEpics = taskManager.getEpics();
    assertEquals(0, actualEpics.size(), "Size should be equals");
  }

  @Test
  void getHistory() {
    List<Task> history = taskManager.getHistory();
    assertEquals(4, history.size(), "Size should be equals");
  }

  @ParameterizedTest()
  @CsvSource({"2026-01-01T00:00, 1", "2026-01-01T00:14, 15", "2026-01-01T00:14, 15"})
  void validate(String startTime, String duration) {
    Task task2 = new Task(5, "Test 2", "Testing task 2",
        TaskStatus.NEW, LocalDateTime.parse(startTime), Long.parseLong(duration));

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> {
          taskManager.addNewTask(task2);
        });
    String msg = "Время выполнения задачи пересекается со временем уже существующей задачи. "
        + "Выберите другую дату.";
    assertEquals(msg, exception.getMessage());
  }

  @Test
  void setEpicDateTime() {

  }

  @Test
  void getPrioritizedTasks() {
    Task task2 = new Task(5, "Test 5", "Testing task 5",
        TaskStatus.NEW, START_TIME.minusMinutes(15), 15);
    taskManager.addNewTask(task2);
    tasks.add(task2);
    tasks.addAll(subtasks);
    List<Task> sorted = tasks.stream()
        .sorted(Comparator.comparing(Task::getStartTime))
        .collect(Collectors.toList());
    List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
    assertEquals(sorted, prioritizedTasks, "Tasks should be equals");
  }
}