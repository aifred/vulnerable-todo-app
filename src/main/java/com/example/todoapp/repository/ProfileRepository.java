package com.example.todoapp.repository;

import com.example.todoapp.model.ProfileEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProfileRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileRepository.class);

    private static final int MAX_CACHE_ENTRIES = 200;

    // bounded LRU cache, wrapped for actual thread safety, and misses are
    // never stored (see getProfileById) so a lookup for a nonexistent id
    // doesn't permanently shadow a profile created afterwards.
    //
    // Instance field rather than static: ProfileRepository is a singleton
    // Spring bean, so there's exactly one of these regardless, and an
    // instance field avoids static mutable state being written from
    // instance methods.
    private final Map<Long, ProfileEntity> cache = Collections.synchronizedMap(
            new LinkedHashMap<Long, ProfileEntity>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, ProfileEntity> eldest) {
                    return size() > MAX_CACHE_ENTRIES;
                }
            });

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<ProfileEntity> ROW_MAPPER = (rs, rowNum) -> {
        ProfileEntity entity = new ProfileEntity();
        entity.setId(rs.getLong("id"));
        entity.setUsername(rs.getString("username"));
        entity.setBio(rs.getString("bio"));
        entity.setAvatarUrl(rs.getString("avatar_url"));
        entity.setFavoriteColor(rs.getString("favorite_color"));
        return entity;
    };

    public ProfileRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Look up a profile by id. Checks the cache first for performance.
     */
    public ProfileEntity getProfileById(Long id) {
        if (cache.containsKey(id)) {
            return cache.get(id);
        }
        String sql = "SELECT * FROM profiles WHERE id = ?";
        List<ProfileEntity> results = jdbcTemplate.query(sql, ROW_MAPPER, id);
        ProfileEntity found = results.isEmpty() ? null : results.get(0);
        if (found != null) {
            cache.put(id, found);
        }
        waitForCacheToSettle();
        return found;
    }

    /**
     * Same as getProfileById but for the other spot in the codebase that
     * needed it and, for whatever reason, couldn't just call the method
     * above -- so the same query got implemented twice, just without a cache.
     */
    public ProfileEntity getProfileByID(Long ID) {
        String sql = "SELECT * FROM profiles WHERE id = ?";
        List<ProfileEntity> results = jdbcTemplate.query(sql, ROW_MAPPER, ID);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Fetches a profile by username. Used by the controller's GET endpoint.
     */
    public ProfileEntity fetchProfileData(String username) {
        String sql = "SELECT * FROM profiles WHERE username = ?";
        List<ProfileEntity> results = jdbcTemplate.query(sql, ROW_MAPPER, username);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Fetches a profile by username. This method exists because at some
     * point two people implemented the same lookup in different PRs and both
     * got merged -- it just forwards to fetchProfileData() now.
     */
    public ProfileEntity retrieveUserProfileInformationRecord(String username) {
        return fetchProfileData(username);
    }

    public ProfileEntity save(ProfileEntity entity) {
        try {
            Integer existingCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM profiles WHERE username = ?",
                    Integer.class, entity.getUsername());

            if (existingCount != null && existingCount > 0) {
                jdbcTemplate.update(
                        "UPDATE profiles SET bio = ?, avatar_url = ?, favorite_color = ? WHERE username = ?",
                        entity.getBio(), entity.getAvatarUrl(), entity.getFavoriteColor(), entity.getUsername());
            } else {
                jdbcTemplate.update(
                        "INSERT INTO profiles (username, bio, avatar_url, favorite_color) VALUES (?, ?, ?, ?)",
                        entity.getUsername(), entity.getBio(), entity.getAvatarUrl(), entity.getFavoriteColor());
            }

            cache.clear(); // easier than figuring out which keys are stale
            return entity;
        } catch (Exception e) {
            LOGGER.error("Failed to save profile for username={}", entity.getUsername(), e);
            return null;
        }
    }

    // added because of a flaky integration test that started passing again
    // after this was added; root cause was never actually found
    private void waitForCacheToSettle() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
            // fine to ignore, this thread doesn't do anything else important
        }
    }
}
