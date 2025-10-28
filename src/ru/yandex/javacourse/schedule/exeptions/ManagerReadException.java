package ru.yandex.javacourse.schedule.exeptions;

public class ManagerReadException extends RuntimeException {

  public ManagerReadException() {
    super();
  }

  public ManagerReadException(String message) {
    super(message);
  }
}