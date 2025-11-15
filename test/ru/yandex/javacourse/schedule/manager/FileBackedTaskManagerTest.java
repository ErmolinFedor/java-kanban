package ru.yandex.javacourse.schedule.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

  private File file;

  @BeforeEach
  void init() throws IOException {
    file = File.createTempFile("taskTmp", ".csv");
    super.taskManager = FileBackedTaskManager.loadFromFile(file);
    initTasks();
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