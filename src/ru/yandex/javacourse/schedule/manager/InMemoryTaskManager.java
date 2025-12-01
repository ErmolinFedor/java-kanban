package ru.yandex.javacourse.schedule.manager;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.IN_PROGRESS;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Set;
import java.util.TreeSet;
import ru.yandex.javacourse.schedule.exeptions.OverlapTasksException;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

public class InMemoryTaskManager implements TaskManager {

	protected final Map<Integer, Task> tasks = new HashMap<>();
	protected final Map<Integer, Epic> epics = new HashMap<>();
	protected final Map<Integer, Subtask> subtasks = new HashMap<>();
	protected int generatorId = 0;
	protected final HistoryManager historyManager = Managers.getDefaultHistory();
	protected Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime,
					Comparator.nullsLast(Comparator.naturalOrder()))
			.thenComparing(Task::getId));


	@Override
	public ArrayList<Task> getTasks() {
		return new ArrayList<>(this.tasks.values());
	}

	@Override
	public ArrayList<Subtask> getSubtasks() {
		return new ArrayList<>(subtasks.values());
	}

	@Override
	public ArrayList<Epic> getEpics() {
		return new ArrayList<>(epics.values());
	}

	@Override
	public ArrayList<Subtask> getEpicSubtasks(int epicId) {
		ArrayList<Subtask> tasks = new ArrayList<>();
		Epic epic = epics.get(epicId);
		if (isNull(epic)) {
			return null;
		}
		for (int id : epic.getSubtaskIds()) {
			tasks.add(subtasks.get(id));
		}
		return tasks;
	}

	@Override
	public Task getTask(int id) {
		final Task task = tasks.get(id);
		historyManager.add(task);
		return task;
	}

	@Override
	public Subtask getSubtask(int id) {
		final Subtask subtask = subtasks.get(id);
		historyManager.add(subtask);
		return subtask;
	}

	@Override
	public Epic getEpic(int id) {
		final Epic epic = epics.get(id);
		historyManager.add(epic);
		return epic;
	}

	@Override
	public int addNewTask(Task task) {
    if (!validate(task)) {
      throw new OverlapTasksException();
    }
		final int id = ++generatorId;
		task.setId(id);
		tasks.put(id, task);
		prioritizedTasks.add(task);
		return id;
  }

	@Override
	public int addNewEpic(Epic epic) {
		final int id = ++generatorId;
		epic.setId(id);
		epics.put(id, epic);
		return id;

	}

	@Override
	public Integer addNewSubtask(Subtask subtask) {
    if (!validate(subtask)) {
      throw new OverlapTasksException();
    }
		final int epicId = subtask.getEpicId();
		Epic epic = epics.get(epicId);
		if (epic == null) {
			return null;
		}
		final int id = ++generatorId;
		subtask.setId(id);
		subtasks.put(id, subtask);
		epic.addSubtaskId(subtask.getId());
		updateEpicStatus(epicId);
		setEpicDateTime(epicId);
		prioritizedTasks.add(subtask);
		return id;
  }

	@Override
	public void updateTask(Task task) {
		if (validate(task)) {
			final int id = task.getId();
			final Task savedTask = tasks.get(id);
			if (savedTask == null) {
				return;
			}
			tasks.put(id, task);
		}
	}

	@Override
	public void updateEpic(Epic epic) {
		final Epic savedEpic = epics.get(epic.getId());
		savedEpic.setName(epic.getName());
		savedEpic.setDescription(epic.getDescription());
	}

	@Override
	public List<Task> getPrioritizedTasks() {
		return new ArrayList<>(prioritizedTasks);
	}

	@Override
	public void updateSubtask(Subtask subtask) {
		if (validate(subtask)) {
			final int id = subtask.getId();
			final int epicId = subtask.getEpicId();
			final Subtask savedSubtask = subtasks.get(id);
			if (savedSubtask == null) {
				return;
			}
			final Epic epic = epics.get(epicId);
			if (epic == null) {
				return;
			}
			subtasks.put(id, subtask);
			updateEpicStatus(epicId);
			setEpicDateTime(epicId);
		}

	}

	@Override
	public void deleteTask(int id) {
		prioritizedTasks.remove(tasks.get(id));
		tasks.remove(id);
	}

	@Override
	public void deleteEpic(int id) {
		final Epic epic = epics.remove(id);
		historyManager.remove(id);
    epic.getSubtaskIds().forEach(subtaskId -> {
      prioritizedTasks.remove(subtasks.get(subtaskId));
      subtasks.remove(subtaskId);
      historyManager.remove(subtaskId);
    });
	}

	@Override
	public void deleteSubtask(int id) {
		prioritizedTasks.remove(subtasks.get(id));
		Subtask subtask = subtasks.remove(id);
		if (isNull(subtask)) {
			return;
		}
		Epic epic = epics.get(subtask.getEpicId());
		epic.removeSubtask(id);
		setEpicDateTime(epic.getId());
		historyManager.remove(id);
		updateEpicStatus(epic.getId());
	}

	@Override
	public void deleteTasks() {
    tasks.keySet().forEach(id -> {
      historyManager.remove(id);
      prioritizedTasks.remove(tasks.get(id));
    });
		tasks.clear();
	}

	@Override
	public void deleteSubtasks() {
		for (Epic epic : epics.values()) {
			epic.cleanSubtaskIds();
			updateEpicStatus(epic.getId());
			setEpicDateTime(epic.getId());
			historyManager.remove(epic.getId());
		}
    subtasks.keySet().forEach(id -> {
      historyManager.remove(id);
      prioritizedTasks.remove(subtasks.get(id));
    });
		subtasks.clear();
	}

	@Override
	public void deleteEpics() {
		for (Epic epic : epics.values()) {
			List<Integer> subtaskIds = epics.get(epic.getId()).getSubtaskIds();
      subtaskIds.forEach(subtaskId -> {
				prioritizedTasks.remove(subtasks.get(subtaskId));
				subtasks.remove(subtaskId);
				historyManager.remove(subtaskId);
      });
			historyManager.remove(epic.getId());
		}
		epics.clear();
		subtasks.clear();
	}

	@Override
	public List<Task> getHistory() {
		return historyManager.getHistory();
	}

	private void updateEpicStatus(int epicId) {
		Epic epic = epics.get(epicId);
		List<Integer> subs = epic.getSubtaskIds();
		if (subs.isEmpty()) {
			epic.setStatus(NEW);
			return;
		}
		TaskStatus status = null;
		for (int id : subs) {
			final Subtask subtask = subtasks.get(id);
			if (isNull(status)) {
				status = subtask.getStatus();
				continue;
			}

			if (status == subtask.getStatus()
					&& status != IN_PROGRESS) {
				continue;
			}
			epic.setStatus(IN_PROGRESS);
			return;
		}
		epic.setStatus(status);
	}

	@Override
	public void setEpicDateTime(int epicId) {
		List<Integer> subtaskIds = epics.get(epicId).getSubtaskIds();
		if (subtaskIds.isEmpty()) {
			epics.get(epicId).setDuration(0L);
			epics.get(epicId).setStartTime(null);
			epics.get(epicId).setEndTime(null);
			return;
		}
		LocalDateTime epicStartTime = null;
		LocalDateTime epicEndTime = null;
		long epicDuration = 0L;
		for (Integer subtaskId : subtaskIds) {
			Subtask subtask = subtasks.get(subtaskId);
			LocalDateTime subtaskStartTime = subtask.getStartTime();
			LocalDateTime subtaskEndTime = subtask.getEndTime();
			if (nonNull(subtaskStartTime)) {
				if (isNull(epicStartTime) || subtaskStartTime.isBefore(epicStartTime)) {
					epicStartTime = subtaskStartTime;
				}
			}
			if (nonNull(subtaskEndTime)) {
				if (isNull(epicEndTime) || subtaskEndTime.isAfter(epicEndTime)) {
					epicEndTime = subtaskEndTime;
				}
			}
			epicDuration += subtasks.get(subtaskId).getDuration();
		}
		epics.get(epicId).setStartTime(epicStartTime);
		epics.get(epicId).setEndTime(epicEndTime);
		epics.get(epicId).setDuration(epicDuration);
	}

	@Override
	public boolean validate(Task task) {
		List<Task> prioritizedTasks = getPrioritizedTasks();

    return prioritizedTasks.stream()
        .takeWhile(existTask ->
						nonNull(task.getStartTime()) && nonNull(existTask.getStartTime()))
        .filter(existTask -> task.getId() != existTask.getId())
				.noneMatch(existTask ->
            (task.getEndTime().isAfter(existTask.getStartTime())
								&& task.getStartTime().isBefore(existTask.getEndTime()))
								||
                (task.getStartTime().isBefore(existTask.getEndTime())
										&& task.getStartTime().isAfter(existTask.getEndTime())));
  }
}