package ru.yandex.javacourse.schedule.tasks;

public class Subtask extends Task {
	protected int epicId;

	private final TaskType taskType = TaskType.SUBTASK;

	public Subtask(int id, String name, String description, TaskStatus status, int epicId) {
		super(id, name, description, status);
		this.epicId = epicId;
	}

	public Subtask(String name, String description, TaskStatus status, int epicId) {
		super(name, description, status);
		this.epicId = epicId;
	}

	public int getEpicId() {
		return epicId;
	}

	@Override
	public String toString() {
		return "Subtask{" +
				"id=" + id +
				", epicId=" + epicId +
				", name='" + name + '\'' +
				", status=" + status +
				", description='" + description + '\'' +
				'}';
	}

  @Override
  public TaskType getTaskType() {
    return taskType;
  }
}