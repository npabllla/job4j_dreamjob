package ru.job4j.dream.store;

import ru.job4j.dream.model.Post;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Store {

    private static final Store INST = new Store();

    private final Map<Integer, Post> posts = new ConcurrentHashMap<>();

    private Store() {
        posts.put(1, new Post(1, "Junior Java Job", "Some description", LocalDate.of(2021, 3, 15)));
        posts.put(2, new Post(2, "Middle Java Job", "Some description", LocalDate.of(2021, 2, 10)));
        posts.put(3, new Post(3, "Senior Java Job", "Some description", LocalDate.of(2021, 1, 5)));
    }

    public static Store instOf() {
        return INST;
    }

    public Collection<Post> findAll() {
        return posts.values();
    }
}