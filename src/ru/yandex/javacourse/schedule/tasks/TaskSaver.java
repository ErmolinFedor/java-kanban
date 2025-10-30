package ru.yandex.javacourse.schedule.tasks;

public enum TaskSaver {
  ID(0),
  TYPE(1),
  NAME(2),
  STATUS(3),
  DESCRIPTION(4),
  EPIC(5);

  private final int index;

  TaskSaver(int index) {
    this.index = index;
  }

  public int getIndex() {
    return index;
  }
}