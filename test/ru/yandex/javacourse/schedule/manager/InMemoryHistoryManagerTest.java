package ru.yandex.javacourse.schedule.manager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.DONE;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.IN_PROGRESS;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

public class InMemoryHistoryManagerTest {

    protected final LocalDateTime START_DATE = LocalDateTime.of(2026, 1, 1, 0, 0);
    HistoryManager historyManager;
    private List<Task> tasks;

    @BeforeEach
    public void initHistoryManager(){
        historyManager = Managers.getDefaultHistory();
        tasks = new ArrayList<>();
    }

    @Test
    public void testHistoricVersions() {
        Task task = new Task(1,"Test 1", "Testing task 1",
            NEW, START_DATE, 15);
        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size(),
            "historic task should be added");
        task.setStatus(IN_PROGRESS);
        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size(),
            "historic task should not be added");
    }

    @Test
    public void testHistoricVersionsByPointer() {
        Task task = new Task(1,"Test 1", "Testing task 1",
            NEW, START_DATE, 15);
        historyManager.add(task);
        assertEquals(task.getStatus(), historyManager.getHistory().get(0).getStatus(),
            "historic task should be stored");
        task.setStatus(IN_PROGRESS);
        historyManager.add(task);
        assertEquals(IN_PROGRESS, historyManager.getHistory().get(0).getStatus(),
            "historic task should not be changed");
    }

    @Test
    public void testHistoricVersionsWithDuplicate() {
        Task task = new Task(1,"Test 1", "Testing task 1",
            NEW, START_DATE, 15);
        historyManager.add(task);
        assertEquals(task.getStatus(), historyManager.getHistory().get(0).getStatus(),
            "historic task should be stored");
        Task task1 = new Task(1,"Test 1", "Testing task 1",
            NEW, START_DATE, 15);
        historyManager.add(task1);
        assertEquals(NEW, historyManager.getHistory().get(0).getStatus(),
            "historic task should not be changed");
    }

    @Test
    public void testHistoricVersionsWithNull() {
        Task task = new Task(1,"Test 1", "Testing task 1",
            NEW, START_DATE, 15);
        historyManager.add(task);
        assertEquals(task.getStatus(), historyManager.getHistory().get(0).getStatus(),
            "historic task should be stored");
        historyManager.add(null);
        assertEquals(NEW, historyManager.getHistory().get(0).getStatus(),
            "historic task should not be changed");
    }

    @Test
    public void testHistoricVersionsAddInTheMiddle() {
        Task task1 = new Task(1,"Test 1", "Testing task 1",
            NEW, START_DATE, 15);
        historyManager.add(task1);
        tasks.add(task1);
        Task task2 = new Task(2,"Test 1", "Testing task 1",
            NEW, START_DATE.plusMinutes(30), 15);
        historyManager.add(task2);
        tasks.add(task2);
        Task task3 = new Task(3,"Test 1", "Testing task 1", NEW,
            START_DATE.plusMinutes(15), 15);
        historyManager.add(task3);
        tasks.add(task3);
        List<Task> actual = historyManager.getHistory();
        assertEquals(tasks.size(), actual.size(), "Size should be equals");
        assertEquals(tasks, actual, "Tasks should be equals");
    }

    @Test
    public void testHistoricVersionsRemoveInTheMiddle() {
        Task task1 = new Task(1,"Test 1", "Testing task 1", NEW, START_DATE, 15);
        tasks.add(task1);
        historyManager.add(task1);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be added");
        Task task2 = new Task(2,"Test 2", "Testing task 2", IN_PROGRESS, START_DATE.minusMinutes(15), 15);
        tasks.add(task2);
        historyManager.add(task2);
        Task task3 = new Task(3,"Test 2", "Testing task 2", DONE, START_DATE.minusMinutes(15), 15);
        tasks.add(task3);
        historyManager.add(task3);
        assertEquals(tasks.size(),  historyManager.getHistory().size(), "Size should be equals");
        assertEquals(tasks,  historyManager.getHistory(), "Tasks should be equals");
        historyManager.remove(task2.getId());
        tasks.remove(task2);
        assertEquals(tasks.size(),  historyManager.getHistory().size(), "Size should be equals");
        assertEquals(tasks,  historyManager.getHistory(), "Tasks should be equals");
    }

    @Test
    public void testHistoricVersionsRemoveFirst() {
        Task task1 = new Task(1,"Test 1", "Testing task 1", NEW, START_DATE, 15);
        tasks.add(task1);
        historyManager.add(task1);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be added");
        Task task2 = new Task(2,"Test 2", "Testing task 2", IN_PROGRESS, START_DATE.minusMinutes(15), 15);
        tasks.add(task2);
        historyManager.add(task2);
        Task task3 = new Task(3,"Test 2", "Testing task 2", DONE, START_DATE.minusMinutes(15), 15);
        tasks.add(task3);
        historyManager.add(task3);
        assertEquals(tasks.size(),  historyManager.getHistory().size(), "Size should be equals");
        assertEquals(tasks,  historyManager.getHistory(), "Tasks should be equals");
        historyManager.remove(task1.getId());
        tasks.remove(task1);
        assertEquals(tasks.size(),  historyManager.getHistory().size(), "Size should be equals");
        assertEquals(tasks,  historyManager.getHistory(), "Tasks should be equals");
    }

    @Test
    public void testHistoricVersionsRemoveLast() {
        Task task1 = new Task(1,"Test 1", "Testing task 1", NEW, START_DATE, 15);
        tasks.add(task1);
        historyManager.add(task1);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be added");
        Task task2 = new Task(2,"Test 2", "Testing task 2", DONE, START_DATE.minusMinutes(15), 15);
        tasks.add(task2);
        historyManager.add(task2);
        Task task3 = new Task(3,"Test 2", "Testing task 2", IN_PROGRESS, START_DATE.minusMinutes(15), 15);
        tasks.add(task3);
        historyManager.add(task3);
        assertEquals(tasks.size(),  historyManager.getHistory().size(), "Size should be equals");
        assertEquals(tasks,  historyManager.getHistory(), "Tasks should be equals");
        historyManager.remove(task3.getId());
        tasks.remove(task3);
        assertEquals(tasks.size(),  historyManager.getHistory().size(), "Size should be equals");
        assertEquals(tasks,  historyManager.getHistory(), "Tasks should be equals");
    }
}