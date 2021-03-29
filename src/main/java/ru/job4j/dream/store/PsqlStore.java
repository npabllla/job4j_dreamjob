package ru.job4j.dream.store;

import org.apache.commons.dbcp2.BasicDataSource;
import ru.job4j.dream.model.Candidate;
import ru.job4j.dream.model.City;
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
             PreparedStatement ps =  cn.prepareStatement("SELECT can.id,"
                     + "can.name,"
                     + "c.city_name "
                     + "FROM candidate as can join cities as c on can.city_id = c.id")
        ) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    candidates.add(new Candidate(it.getInt("id"),
                            it.getString("name"),
                            it.getString("city_name")));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception", e);
        }
        return candidates;
    }

    @Override
    public Collection<City> findAllCity() {
        List<City> cities = new ArrayList<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("select * from cities")) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    cities.add(new City(it.getInt("id"),
                            it.getString("city_name")));
                }
            }
        } catch (Exception e) {
            LOG.warn("Exception", e);
        }
        return cities;
    }

    @Override
    public User findByEmail(String email) {
        User user = null;
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("select * from users where email=?")
        ) {
            ps.setString(1, email);
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    user = new User(it.getInt("id"), it.getString("name"));
                    user.setEmail(it.getString("email"));
                    user.setPassword(it.getString("password"));
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
            try (Connection cn = pool.getConnection();
                 PreparedStatement ps =  cn.prepareStatement("INSERT INTO post(name) VALUES (?)",
                         PreparedStatement.RETURN_GENERATED_KEYS)
            ) {
                ps.setString(1, post.getName());
                ps.execute();
                try (ResultSet id = ps.getGeneratedKeys()) {
                    if (id.next()) {
                        post.setId(id.getInt(1));
                    }
                }
            } catch (Exception e) {
                LOG.error("Exception", e);
            }
        } else {
            try (Connection cn = pool.getConnection();
                 PreparedStatement ps =  cn.prepareStatement("update post set name =? where id=?",
                         PreparedStatement.RETURN_GENERATED_KEYS)
            ) {
                ps.setString(1, post.getName());
                ps.setInt(2, post.getId());
                ps.executeUpdate();
            } catch (Exception e) {
                LOG.error("Exception", e);
            }
        }
    }

    @Override
    public void save(Candidate candidate) {
        if (candidate.getId() == 0) {
            try (Connection cn = pool.getConnection();
                 PreparedStatement ps =  cn.prepareStatement("INSERT INTO candidate(name, city_id)"
                         + "VALUES (?, (SELECT id from cities where name = ?))",
                         PreparedStatement.RETURN_GENERATED_KEYS)
            ) {
                ps.setString(1, candidate.getName());
                ps.setString(2, candidate.getCityName());
                ps.execute();
                try (ResultSet id = ps.getGeneratedKeys()) {
                    if (id.next()) {
                        candidate.setId(id.getInt(1));
                    }
                }
            } catch (Exception e) {
                LOG.error("Exception", e);
            }
        } else {
            try (Connection cn = pool.getConnection();
                 PreparedStatement ps =  cn.prepareStatement("UPDATE candidate set name = ?,"
                         + "city_id = (SELECT id from cities where name = ?) where id = ?;",
                         PreparedStatement.RETURN_GENERATED_KEYS)
            ) {
                ps.setString(1, candidate.getName());
                ps.setString(2, candidate.getCityName());
                ps.setInt(3, candidate.getId());
                ps.executeUpdate();
            } catch (Exception e) {
                LOG.error("Exception", e);
            }
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
                ps.setInt(4, user.getId());
                ps.executeUpdate();
            } catch (Exception e) {
                LOG.error("Exception", e);
            }
        }
    }

    @Override
    public Post findPostById(int id) {
        Post post = null;
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("SELECT * FROM post where id=?")
        ) {
            ps.setInt(1, id);
            try (ResultSet it = ps.executeQuery()) {
                if (it.next()) {
                    post = new Post(it.getInt("id"),
                            it.getString("name"));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception", e);
        }
        return post;
    }

    @Override
    public Candidate findCandidateById(int id) {
        Candidate candidate = null;
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("SELECT * FROM candidate join cities c "
                     + "on c.id = candidate.city_id where candidate.id =?")
        ) {
            ps.setInt(1, id);
            try (ResultSet it = ps.executeQuery()) {
                if (it.next()) {
                    candidate = new Candidate(it.getInt("id"),
                            it.getString("name"),
                            it.getString("city_name"));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception", e);
        }
        return candidate;
    }
}