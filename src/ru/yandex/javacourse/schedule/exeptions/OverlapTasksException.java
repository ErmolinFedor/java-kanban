package ru.yandex.javacourse.schedule.exeptions;

public class OverlapTasksException extends RuntimeException {
  public OverlapTasksException() {
    super("Время выполнения задачи пересекается со временем уже существующей задачи. "
        + "Выберите другую дату.");
  }
}