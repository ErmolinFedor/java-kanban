package ru.yandex.javacourse.schedule.tasks;

import java.time.LocalDateTime;

public class Subtask extends Task {
	protected int epicId;

	public Subtask(int id, String name, String description, TaskStatus status, int epicId,
			LocalDateTime startTime, long duration) {
		super(id, name, description, status, startTime, duration);
		this.epicId = epicId;
		taskType = TaskType.SUBTASK;
	}

	public Subtask(String name, String description, TaskStatus status, int epicId,
			LocalDateTime startTime, long duration) {
		super(name, description, status, startTime, duration);
		this.epicId = epicId;
		taskType = TaskType.SUBTASK;
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
				", startTime='" + startTime.format(formatter) + '\'' +
				", duration='" + duration + '\'' +
				'}';
	}

  @Override
  public TaskType getTaskType() {
    return taskType;
  }

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}

		Subtask subtask = (Subtask) o;
		return epicId == subtask.epicId && taskType == subtask.taskType;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + epicId;
		result = 31 * result + taskType.hashCode();
		return result;
	}
}