package ru.job4j.dream.store;

import ru.job4j.dream.model.Candidate;
import ru.job4j.dream.model.Post;
import ru.job4j.dream.model.User;

import java.util.Collection;

public interface Store {
    Collection<Post> findAllPosts();

    Collection<Candidate> findAllCandidates();

    User findByEmail(String email);

    void save(Post post);

    void save(Candidate candidate);

    void save(User user);

    Post findPostById(int id);

    Candidate findCandidateById(int id);
}