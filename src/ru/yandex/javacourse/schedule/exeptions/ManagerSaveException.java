package ru.yandex.javacourse.schedule.exeptions;

public class ManagerSaveException extends RuntimeException {

  public ManagerSaveException(String message) {
    super(message);
  }
}