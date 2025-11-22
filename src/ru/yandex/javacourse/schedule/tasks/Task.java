package ru.yandex.javacourse.schedule.tasks;

import static java.util.Objects.nonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task {
	protected int id;
	protected String name;
	protected TaskStatus status;
	protected String description;
	private final TaskType taskType = TaskType.TASK;
	protected LocalDateTime startTime;
	protected long duration;

	public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

	public Task(int id, String name, String description, TaskStatus status, LocalDateTime startTime, long duration) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.status = status;
		this.startTime = startTime;
		this.duration = duration;
	}

	public Task(String name, String description, TaskStatus status, LocalDateTime startTime, long duration) {
		this.name = name;
		this.description = description;
		this.status = status;
		this.startTime = startTime;
		this.duration = duration;
	}

	public Task(int id, String name, String description, TaskStatus status) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.status = status;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

  public TaskType getTaskType() {
    return taskType;
  }

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public LocalDateTime getEndTime() {
		return nonNull(startTime) ? startTime.plusMinutes(duration) : null;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Task task = (Task) o;
		return id == task.id && duration == task.duration && name.equals(task.name)
				&& status == task.status && description.equals(task.description)
				&& taskType == task.taskType
				&& Objects.equals(startTime, task.startTime);
	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + name.hashCode();
		result = 31 * result + status.hashCode();
		result = 31 * result + description.hashCode();
		result = 31 * result + taskType.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "Task{" +
				"id=" + id +
				", name='" + name + '\'' +
				", status='" + status + '\'' +
				", description='" + description + '\'' +
				", startTime='" + startTime.format(formatter) + '\'' +
				", duration='" + duration + '\'' +
				'}';
	}
}