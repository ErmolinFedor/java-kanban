package ru.yandex.javacourse.schedule.tasks;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
	protected ArrayList<Integer> subtaskIds = new ArrayList<>();
	private LocalDateTime endTime;

	private final TaskType taskType = TaskType.EPIC;

	public Epic(int id, String name, String description, LocalDateTime startTime, long duration) {
		super(id, name, description, NEW, startTime, duration);
	}

	public Epic(int id, String name, String description, TaskStatus status, LocalDateTime startTime, long duration) {
		super(id, name, description, status, startTime, duration);
	}

	public Epic(String name, String description, LocalDateTime startTime, long duration) {
		super(name, description, NEW, startTime, duration);
	}

	public Epic(int id, String name, String description) {
		super(id, name, description, NEW);
	}

	public Epic(String name, String description) {
		super(name, description, NEW);
	}

	public Epic(int id, String name, String description, TaskStatus status) {
		super(id, name, description, status);
	}

	public void addSubtaskId(int id) {
		subtaskIds.add(id);
	}

	public List<Integer> getSubtaskIds() {
		return subtaskIds;
	}

	public void cleanSubtaskIds() {
		subtaskIds.clear();
	}

	public void removeSubtask(int id) {
		subtaskIds.remove(Integer.valueOf(id));
	}

	@Override
	public TaskType getTaskType() {
		return taskType;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	@Override
	public String toString() {
		return "Epic{" +
				"id=" + id +
				", name='" + name + '\'' +
				", status=" + status +
				", description='" + description + '\'' +
				", subtaskIds=" + subtaskIds +
				", startTime='" + startTime.format(formatter) + '\'' +
				", duration='" + duration + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}

		Epic epic = (Epic) o;
		return subtaskIds.equals(epic.subtaskIds) && taskType == epic.taskType;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + subtaskIds.hashCode();
		result = 31 * result + endTime.hashCode();
		result = 31 * result + taskType.hashCode();
		return result;
	}
}