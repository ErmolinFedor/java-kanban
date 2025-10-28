package ru.yandex.javacourse.schedule.exeptions;

public class ManagerSaveException extends RuntimeException {

  public ManagerSaveException() {
    super();
  }

  public ManagerSaveException(String message) {
    super(message);
  }
}