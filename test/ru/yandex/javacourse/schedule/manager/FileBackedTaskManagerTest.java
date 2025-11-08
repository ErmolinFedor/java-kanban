package ru.yandex.javacourse.schedule.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.DONE;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

public class FileBackedTaskManagerTest {

  private File file;
  private FileBackedTaskManager taskManager;

  @BeforeEach
  void init() throws IOException, IllegalAccessException, NoSuchFieldException {
    file = File.createTempFile("taskTmp", ".csv");
    taskManager = FileBackedTaskManager.loadFromFile(file);

    Task task = new Task("Task1", "Description task1", TaskStatus.NEW);
    taskManager.addNewTask(task);
    Epic epic = new Epic("Epic1", "Description epic1");
    taskManager.addNewEpic(epic);
    Subtask subtask = new Subtask("Subtask #1-1", "Description sub task#1-1", NEW, epic.getId());
    Integer newSubtask = taskManager.addNewSubtask(subtask);
    Subtask subtask1 = new Subtask("Subtask #2-1", "Description sub task#2-1", DONE, epic.getId());
    Integer newSubtask1 = taskManager.addNewSubtask(subtask1);
    taskManager.getTask(task.getId());
    taskManager.getEpic(epic.getId());
    taskManager.getSubtask(newSubtask);
    taskManager.getSubtask(newSubtask1);
  }

  @Test
  void testLoadFromFile() {
    FileBackedTaskManager fileManager = FileBackedTaskManager.loadFromFile(
        file = new File("./resources/test.csv"));
    assertEqualsManager(fileManager);
  }

  @Test
  void testSaveToFile(){
    assertEqualsManager(FileBackedTaskManager.loadFromFile(file));
  }

  private void assertEqualsManager(FileBackedTaskManager actual) {
    assertEquals(taskManager.getTasks(), actual.getTasks(),
        "Tasks size is not equals");
    assertEquals(taskManager.getEpics(), actual.getEpics(),
        "Epics size is not equals");
    assertEquals(taskManager.getSubtasks(), actual.getSubtasks(),
        "Subtasks size is not equals");
  }
}