package ru.yandex.javacourse.schedule.manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import ru.yandex.javacourse.schedule.exeptions.ManagerReadException;
import ru.yandex.javacourse.schedule.exeptions.ManagerSaveException;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;
import ru.yandex.javacourse.schedule.tasks.TaskType;

public class FileBackedTaskManager extends InMemoryTaskManager {

  private final File file = new File("./resources/tasks.csv");
  private static final String FIRST_LINE = "id,type,name,status,description,epic";

  protected void save() {
    try (BufferedWriter writer = new BufferedWriter(
        new FileWriter(file, StandardCharsets.UTF_8))) {
      writer.write(FIRST_LINE);
      addTasksToFile(writer);
    } catch (IOException e) {
      throw new ManagerSaveException(e.getMessage());
    }
  }

  private void addTasksToFile(BufferedWriter writer) throws IOException {
    for (Task task : getTasks()) {
      writer.newLine();
      writer.write(toString(task));
    }
    for (Epic epic : getEpics()) {
      writer.newLine();
      writer.write(toString(epic));
    }
    for (Subtask subtask : getSubtasks()) {
      writer.newLine();
      writer.write(toString(subtask));
    }
  }

  protected static FileBackedTaskManager loadFromFile(File file) {
    FileBackedTaskManager fileManager = new FileBackedTaskManager();
    try (BufferedReader reader = new BufferedReader(
        new FileReader(file, StandardCharsets.UTF_8))) {
      List<String> fileLines = reader.lines().toList();
      for (int i = 1; i < fileLines.size(); i++) {
        putTask(fileLines.get(i).split(","), fileManager);
      }
    } catch (IOException e) {
      throw new ManagerReadException(e.getMessage());
    }
    return fileManager;
  }

  private static void putTask(String[] taskLines, FileBackedTaskManager fileManager) {
    Task task = fromString(taskLines);
    switch (task.getTaskType()) {
      case TASK:
        fileManager.tasks.put(task.getId(), task);
        break;
      case EPIC:
        fileManager.epics.put(task.getId(), (Epic) task);
        break;
      case SUBTASK:
        fileManager.subtasks.put(task.getId(), (Subtask) task);
        int epicId = ((Subtask) task).getEpicId();
        List<Integer> subtaskIds = fileManager.epics.get(epicId).getSubtaskIds();
        subtaskIds.add(task.getId());
        break;
    }
  }

  private String toString(Task task) {
    return String.format("%d,%S,%s,%s,%s,", task.getId(), task.getTaskType(),
        task.getName(), task.getStatus(), task.getDescription());
  }

  private String toString(Epic epic) {
    return String.format("%d,%S,%s,%s,%s,", epic.getId(), epic.getTaskType(),
        epic.getName(), epic.getStatus(), epic.getDescription());
  }

  private String toString(Subtask subtask) {
    return String.format("%d,%S,%s,%s,%s,%s", subtask.getId(), subtask.getTaskType(),
        subtask.getName(), subtask.getStatus(), subtask.getDescription(), subtask.getEpicId());
  }

  private static Task fromString(String[] line) {
    int id = Integer.parseInt(line[0]);
    TaskType taskType = TaskType.valueOf(line[1]);
    String name = line[2];
    TaskStatus status = TaskStatus.valueOf(line[3]);
    String description = line[4];
    return switch (taskType) {
      case TASK -> new Task(id, name, description, status);
      case EPIC -> new Epic(id, name, description, status);
      case SUBTASK -> {
        int epicId = Integer.parseInt(line[5]);
        yield new Subtask(id, name, description, status, epicId);
      }
    };
  }

  @Override
  public int addNewTask(Task task) {
    int newTask = super.addNewTask(task);
    save();
    return newTask;
  }

  @Override
  public int addNewEpic(Epic epic) {
    int newEpic = super.addNewEpic(epic);
    save();
    return newEpic;
  }

  @Override
  public Integer addNewSubtask(Subtask subtask) {
    Integer newSubtask = super.addNewSubtask(subtask);
    save();
    return newSubtask;
  }

  @Override
  public void updateTask(Task task) {
    super.updateTask(task);
    save();
  }

  @Override
  public void updateEpic(Epic epic) {
    super.updateEpic(epic);
    save();
  }

  @Override
  public void updateSubtask(Subtask subtask) {
    super.updateSubtask(subtask);
    save();
  }

  @Override
  public void deleteTask(int id) {
    super.deleteTask(id);
    save();
  }

  @Override
  public void deleteEpic(int id) {
    super.deleteEpic(id);
    save();
  }

  @Override
  public void deleteSubtask(int id) {
    super.deleteSubtask(id);
    save();
  }

  @Override
  public void deleteTasks() {
    super.deleteTasks();
    save();
  }

  @Override
  public void deleteSubtasks() {
    super.deleteSubtasks();
    save();
  }

  @Override
  public void deleteEpics() {
    super.deleteEpics();
    save();
  }
}