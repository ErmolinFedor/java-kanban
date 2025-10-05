package ru.yandex.javacourse.schedule.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import ru.yandex.javacourse.schedule.tasks.Task;

/**
 * In memory history manager.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public class InMemoryHistoryManager implements HistoryManager {
	private final HistoryLinkedList history = new HistoryLinkedList();

	@Override
	public List<Task> getHistory() {
		return history.getTasks();
	}

	@Override
	public void add(Task task) {
		history.linkLast(task);
	}

	@Override
	public void remove(int id) {
		history.removeNode(id);
	}

	private static class HistoryLinkedList {

		final private Map<Integer, Node<Task>> nodeMap = new HashMap<>();
		private Node<Task> head;
		private Node<Task> tail;

		private List<Task> getTasks() {
			List<Task> tasks = new ArrayList<>();
			for (Node<Task> node = head; node != null; node = node.next) {
				tasks.add(node.task);
			}
			return tasks;
		}

		public void linkLast(Task task) {
			if (nodeMap.containsKey(task.getId())) {
				removeNode(nodeMap.get(task.getId()));
			}
			final Node<Task> oldTail = tail;
			final Node<Task> newNode = new Node<>(oldTail, task, null);
			tail = newNode;
			if (oldTail == null) {
				head = newNode;
			} else {
				oldTail.next = newNode;
			}
			nodeMap.put(task.getId(), newNode);
		}

		private void removeNode(int id) {
			if (nodeMap.containsKey(id)) {
				removeNode(nodeMap.get(id));
			}
		}

		private void removeNode(Node<Task> node) {
			final Node<Task> prev = node.prev;
			final Node<Task> next = node.next;
			if (prev == null) {
				head = next;
			} else {
				prev.next = next;
				node.prev = null;
			}
			if (next == null) {
				tail = prev;
			} else {
				next.prev = prev;
				node.next = null;
			}
			node.task = null;
		}
	}
}