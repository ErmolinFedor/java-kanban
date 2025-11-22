package ru.yandex.javacourse.schedule.manager;

import static ru.yandex.javacourse.schedule.tasks.TaskSaver.DESCRIPTION;
import static ru.yandex.javacourse.schedule.tasks.TaskSaver.DURATION;
import static ru.yandex.javacourse.schedule.tasks.TaskSaver.EPIC;
import static ru.yandex.javacourse.schedule.tasks.TaskSaver.ID;
import static ru.yandex.javacourse.schedule.tasks.TaskSaver.NAME;
import static ru.yandex.javacourse.schedule.tasks.TaskSaver.START_TIME;
import static ru.yandex.javacourse.schedule.tasks.TaskSaver.STATUS;
import static ru.yandex.javacourse.schedule.tasks.TaskSaver.TYPE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.yandex.javacourse.schedule.exeptions.ManagerReadException;
import ru.yandex.javacourse.schedule.exeptions.ManagerSaveException;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;
import ru.yandex.javacourse.schedule.tasks.TaskType;

public class FileBackedTaskManager extends InMemoryTaskManager {

  File file;
  private static final String FIRST_LINE = "id,type,name,status,description,epic,startTime,duration";

  public static FileBackedTaskManager getDeafaultFileBackedTaskManager() {
    File file = new File("./resources/tasks.csv");
    if (!file.exists()) {
      try {
        Files.createFile(file.toPath());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return loadFromFile(file);
  }

  public FileBackedTaskManager(File file) {
    this.file = file;
  }

  protected void save() {
    try (BufferedWriter writer = new BufferedWriter(
        new FileWriter(file, StandardCharsets.UTF_8))) {
      writer.write(FIRST_LINE);
      addTasksToFile(writer);
      writer.newLine();
      writer.newLine();
      List<String> ids = new ArrayList<>();
      for (Task task : getHistory()) {
        ids.add(String.valueOf(task.getId()));
      }
      writer.write(String.join(",", ids));
    } catch (IOException e) {
      throw new ManagerSaveException(e.getMessage());
    }
  }

  private void addTasksToFile(BufferedWriter writer) throws IOException {
    for (Task task : tasks.values()) {
      writer.newLine();
      writer.write(toString(task));
    }
    for (Epic epic : epics.values()) {
      writer.newLine();
      writer.write(toString(epic));
    }
    for (Subtask subtask : subtasks.values()) {
      writer.newLine();
      writer.write(toString(subtask));
    }
  }

  protected static FileBackedTaskManager loadFromFile(File file) {
    final FileBackedTaskManager fileManager = new FileBackedTaskManager(file);
    Map<Integer, Task> fileHistory = new HashMap<>();
    List<Integer> idsHistory = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(
        new FileReader(file, StandardCharsets.UTF_8))) {
      List<String> fileLines = reader.lines().toList();
      for (int i = 1; i < fileLines.size(); i++) {
        if (fileLines.get(i).isEmpty()) {
          if (fileLines.size() > (i + 1)  && !fileLines.get(i + 1).isEmpty()) {
            idsHistory = historyFromString(fileLines.get(i + 1));
          }
          break;
        }

        putTask(fileLines.get(i).split(","), fileManager, fileHistory);
      }
      for (Integer id : idsHistory) {
        fileManager.historyManager.add(fileHistory.get(id));
      }
    } catch (IOException e) {
      throw new ManagerReadException(e.getMessage());
    }
    return fileManager;
  }

  private static void putTask(String[] taskLines, FileBackedTaskManager fileManager, Map<Integer, Task> fileHistory) {
    Task task = fromString(taskLines);
    fileHistory.put(task.getId(), task);
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
    if (task.getId() > fileManager.generatorId) {
      fileManager.generatorId = task.getId();
    }
  }

  private String toString(Task task) {
    return String.format("%d,%S,%s,%s,%s,%s,%s,", task.getId(), task.getTaskType(),
        task.getName(), task.getStatus(), task.getDescription(), task.getStartTime(),
        task.getDuration());
  }

  private String toString(Epic epic) {
    return String.format("%d,%S,%s,%s,%s,%s,%s,", epic.getId(), epic.getTaskType(),
        epic.getName(), epic.getStatus(), epic.getDescription(), epic.getStartTime(),
        epic.getDuration());
  }

  private String toString(Subtask subtask) {
    return String.format("%d,%S,%s,%s,%s,%s,%s,%s", subtask.getId(), subtask.getTaskType(),
        subtask.getName(), subtask.getStatus(), subtask.getDescription(),
        subtask.getStartTime(), subtask.getDuration(), subtask.getEpicId());
  }

  private static Task fromString(String[] line) {
    int id = Integer.parseInt(line[ID.getIndex()]);
    TaskType taskType = TaskType.valueOf(line[TYPE.getIndex()]);
    String name = line[NAME.getIndex()];
    TaskStatus status = TaskStatus.valueOf(line[STATUS.getIndex()]);
    String description = line[DESCRIPTION.getIndex()];
    LocalDateTime startTime = LocalDateTime.parse(line[START_TIME.getIndex()]);
    long duration = Long.parseLong(line[DURATION.getIndex()]);
    return switch (taskType) {
      case TASK -> new Task(id, name, description, status, startTime, duration);
      case EPIC -> new Epic(id, name, description, status, startTime, duration);
      case SUBTASK -> {
        int epicId = Integer.parseInt(line[EPIC.getIndex()]);
        yield new Subtask(id, name, description, status, epicId, startTime, duration);
      }
    };
  }

  private static List<Integer> historyFromString(String value) {
    List<Integer> tasksIds = new ArrayList<>();
    if (value != null) {
      String[] idsString = value.split(",");
      for (String idString : idsString) {
        tasksIds.add(Integer.valueOf(idString));
      }
    }
    return tasksIds;
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

  @Override
  public ArrayList<Task> getTasks() {
    save();
    return super.getTasks();
  }

  @Override
  public ArrayList<Subtask> getSubtasks() {
    save();
    return super.getSubtasks();
  }

  @Override
  public ArrayList<Epic> getEpics() {
    save();
    return super.getEpics();
  }

  @Override
  public ArrayList<Subtask> getEpicSubtasks(int epicId) {
    save();
    return super.getEpicSubtasks(epicId);
  }

  @Override
  public Task getTask(int id) {
    save();
    return super.getTask(id);
  }

  @Override
  public Subtask getSubtask(int id) {
    save();
    return super.getSubtask(id);
  }

  @Override
  public Epic getEpic(int id) {
    save();
    return super.getEpic(id);
  }

  public void setEpicDateTime(int epicId) {
    super.setEpicDateTime(epicId);
    save();
  }
}