package ru.job4j.dream.store;

import org.apache.commons.dbcp2.BasicDataSource;
import ru.job4j.dream.model.Candidate;
import ru.job4j.dream.model.Model;
import ru.job4j.dream.model.Post;
import ru.job4j.dream.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class PsqlStore implements Store {

    private static final Logger LOG = LoggerFactory.getLogger(PsqlStore.class.getName());

    private final BasicDataSource pool = new BasicDataSource();

    private PsqlStore() {
        Properties cfg = new Properties();
        try (BufferedReader io = new BufferedReader(
                new FileReader("db.properties")
        )) {
            cfg.load(io);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        pool.setDriverClassName(cfg.getProperty("jdbc.driver"));
        pool.setUrl(cfg.getProperty("jdbc.url"));
        pool.setUsername(cfg.getProperty("jdbc.username"));
        pool.setPassword(cfg.getProperty("jdbc.password"));
        pool.setMinIdle(5);
        pool.setMaxIdle(10);
        pool.setMaxOpenPreparedStatements(100);
    }

    private static final class Lazy {
        private static final Store INST = new PsqlStore();
    }

    public static Store instOf() {
        return Lazy.INST;
    }

    @Override
    public Collection<Post> findAllPosts() {
        List<Post> posts = new ArrayList<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("SELECT * FROM post")
        ) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    posts.add(new Post(it.getInt("id"), it.getString("name")));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception", e);
        }
        return posts;
    }

    @Override
    public Collection<Candidate> findAllCandidates() {
        List<Candidate> candidates = new ArrayList<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("SELECT * FROM candidate")
        ) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    candidates.add(new Candidate(it.getInt("id"), it.getString("name")));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception", e);
        }
        return candidates;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Optional<User> user = Optional.empty();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("select * from users where email=?")
        ) {
            ps.setString(1, email);
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    user = Optional.of(new User(it.getInt("id"), it.getString("name")));
                    user.get().setEmail(it.getString("email"));
                    user.get().setPassword(it.getString("password"));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception", e);
        }
        return user;
    }

    @Override
    public void save(Post post) {
        if (post.getId() == 0) {
            create(post, "post");
        } else {
            update(post, "post");
        }
    }

    @Override
    public void save(Candidate candidate) {
        if (candidate.getId() == 0) {
            create(candidate, "candidate");
        } else {
            update(candidate, "candidate");
        }
    }

    @Override
    public void save(User user) {
        if (user.getId() == 0) {
            try (Connection cn = pool.getConnection();
                 PreparedStatement ps =  cn.prepareStatement("INSERT INTO users(name, email, password) VALUES (?, ?,?)",
                         PreparedStatement.RETURN_GENERATED_KEYS)
            ) {
                ps.setString(1, user.getName());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPassword());
                ps.execute();
                try (ResultSet id = ps.getGeneratedKeys()) {
                    if (id.next()) {
                        user.setId(id.getInt(1));
                    }
                }
            } catch (Exception e) {
                LOG.error("Exception", e);
            }
        } else {
            try (Connection cn = pool.getConnection();
                 PreparedStatement ps =  cn.prepareStatement("update users set name =?, password =?, "
                         + "email=? where id=?", PreparedStatement.RETURN_GENERATED_KEYS)
            ) {
                ps.setString(1, user.getName());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getEmail());
                ps.executeUpdate();
            } catch (Exception e) {
                LOG.error("Exception", e);
            }
        }
    }

    @Override
    public Optional<Post> findPostById(int id) {
        return (Optional<Post>) findById(id, "post");
    }

    @Override
    public Optional<Candidate> findCandidateById(int id) {
        return (Optional<Candidate>) findById(id, "candidate");
    }

    private Optional<? extends Model> findById(int id, String tableName) {
        Optional<Model> model = Optional.empty();
        String sql = String.format("SELECT * FROM %s WHERE id=%d", tableName, id);
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement(sql)
        ) {
            try (ResultSet it = ps.executeQuery()) {
                if (it.next()) {
                    if (tableName.equals("candidate")) {
                        model = Optional.of(new Candidate(it.getInt("id"), it.getString("name")));
                    } else if (tableName.equals("post")) {
                        model = Optional.of(new Post(it.getInt("id"), it.getString("name")));
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Exception", e);
        }
        return model;
    }

    private Model create(Model model, String tableName) {
        String sql = String.format("INSERT INTO %s(name) VALUES (?)", tableName);
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, model.getName());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    model.setId(id.getInt(1));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception", e);
        }
        return model;
    }

    private void update(Model model, String tableName) {
        String sql = String.format("update %s set name =? where id=?", tableName);
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, model.getName());
            ps.setInt(2, model.getId());
            ps.executeUpdate();
        } catch (Exception e) {
            LOG.error("Exception", e);
        }
    }
}