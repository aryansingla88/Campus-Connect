package com.campus.Campus_Connect.features.visibility.query;

import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.connection.entity.ConnectionStatus;
import com.campus.Campus_Connect.features.connection.entity.UserConnection;
import com.campus.Campus_Connect.features.connection.entity.UserConnectionId;
import com.campus.Campus_Connect.features.metadata.hostels.entity.Hostel;
import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import com.campus.Campus_Connect.features.post.entity.Comment;
import com.campus.Campus_Connect.features.post.entity.Post;
import com.campus.Campus_Connect.features.post.entity.PostImage;
import com.campus.Campus_Connect.features.post.entity.PostTag;
import com.campus.Campus_Connect.features.post.entity.PostVote;
import com.campus.Campus_Connect.features.post.security.PostVisibilityQuerySpecifications;
import com.campus.Campus_Connect.features.visibility.domain.VisibilityContext;
import com.campus.Campus_Connect.features.visibility.domain.VisibilityScope;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisibilitySpecificationsIntegrationTest {

    private static SessionFactory sessionFactory;
    private static EntityManagerFactory entityManagerFactory;
    private static StandardServiceRegistry serviceRegistry;
    private static EntityManager entityManager;
    private static SimpleJpaRepository<VisibilityQueryTestItem, Integer> repository;
    private static SimpleJpaRepository<Post, Integer> postRepository;
    private static final List<String> executedSql = new ArrayList<>();

    @BeforeAll
    static void setUpDatabase() {
        serviceRegistry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DRIVER, "org.h2.Driver")
                .applySetting(AvailableSettings.URL, "jdbc:h2:mem:visibility;DB_CLOSE_DELAY=-1")
                .applySetting(AvailableSettings.USER, "sa")
                .applySetting(AvailableSettings.PASS, "")
                .applySetting(AvailableSettings.DIALECT, "org.hibernate.dialect.H2Dialect")
                .applySetting(AvailableSettings.HBM2DDL_AUTO, "create-drop")
                .applySetting(AvailableSettings.GENERATE_STATISTICS, "true")
                .applySetting(AvailableSettings.STATEMENT_INSPECTOR,
                        (org.hibernate.resource.jdbc.spi.StatementInspector) sql -> {
                            executedSql.add(sql);
                            return sql;
                        })
                .build();
        sessionFactory = new MetadataSources(serviceRegistry)
                .addAnnotatedClass(VisibilityQueryTestItem.class)
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(UserProfile.class)
                .addAnnotatedClass(Hostel.class)
                .addAnnotatedClass(UserConnection.class)
                .addAnnotatedClass(Post.class)
                .addAnnotatedClass(PostImage.class)
                .addAnnotatedClass(PostTag.class)
                .addAnnotatedClass(PostVote.class)
                .addAnnotatedClass(Comment.class)
                .buildMetadata()
                .buildSessionFactory();
        entityManagerFactory = sessionFactory;
        entityManager = entityManagerFactory.createEntityManager();
        repository = new SimpleJpaRepository<>(
                JpaEntityInformationSupport.getEntityInformation(VisibilityQueryTestItem.class, entityManager),
                entityManager
        );
        postRepository = new SimpleJpaRepository<>(
                JpaEntityInformationSupport.getEntityInformation(Post.class, entityManager),
                entityManager
        );
        seedUsersAndRelationships();
    }

    @BeforeEach
    void clearItems() {
        entityManager.clear();
        entityManager.getTransaction().begin();
        entityManager.createQuery("delete from VisibilityQueryTestItem").executeUpdate();
        entityManager.createQuery("delete from Post").executeUpdate();
        entityManager.getTransaction().commit();
        entityManager.clear();
        executedSql.clear();
        sessionFactory.getStatistics().clear();
    }

    @AfterAll
    static void closeDatabase() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        if (serviceRegistry != null) {
            StandardServiceRegistryBuilder.destroy(serviceRegistry);
        }
    }

    @Test
    void connectionsRecognizeBothPairOrderingsAndExcludePendingOrMissingRelationships() {
        persist(
                item(2, "CONNECTIONS", null, "reverse", false),
                item(3, "CONNECTIONS", null, "forward", false),
                item(4, "CONNECTIONS", null, "pending", false),
                item(5, "CONNECTIONS", null, "missing", false)
        );
        VisibilityContext context = context(1, 100, 10, 2, false, Set.of());
        executedSql.clear();
        sessionFactory.getStatistics().clear();

        List<VisibilityQueryTestItem> result = repository.findAll(
                VisibilitySpecifications.visibleTo(context, mapping())
        );

        assertEquals(Set.of("reverse", "forward"), titles(result));
        assertEquals(0, sessionFactory.getStatistics()
                .getEntityStatistics(UserConnection.class.getName()).getLoadCount(),
                "Connection rows should be evaluated in SQL, not loaded as entities per resource.");
        assertTrue(executedSql.stream().anyMatch(sql -> sql.toLowerCase().contains("exists")
                        && sql.toLowerCase().contains("user_friends")),
                "The generated resource query should contain a relational EXISTS check.");
        assertEquals(1, executedSql.stream()
                        .filter(sql -> sql.stripLeading().toLowerCase().startsWith("select"))
                        .count(),
                "Loading a result page must not trigger one relationship query per resource.");
    }

    @Test
    void scopePredicatesMatchResolvedAttributesAndFailClosedForUnknownAttributes() {
        persist(
                item(2, "CLUB", "50", "club-member", false),
                item(2, "CLUB", "51", "club-other", false),
                item(2, "HOSTEL", "10", "hostel-match", false),
                item(2, "HOSTEL", "11", "hostel-other", false),
                item(2, "COURSE", "100", "course-match", false),
                item(2, "COURSE", "101", "course-other", false),
                item(2, "ACADEMIC_YEAR", "2", "year-match", false),
                item(2, "ACADEMIC_YEAR", "3", "year-other", false),
                item(2, "ACADEMIC_YEAR", "ALUMNI", "alumni-unknown", false),
                item(2, "HOSTEL", null, "malformed", false),
                item(2, null, "bad-public", "malformed-public", false)
        );

        Set<String> result = titles(repository.findAll(VisibilitySpecifications.visibleTo(
                context(1, 100, 10, 2, null, Set.of(50)), mapping()
        )));

        assertEquals(Set.of("club-member", "hostel-match", "course-match", "year-match"), result);

        persist(item(2, "ACADEMIC_YEAR", "ALUMNI", "alumni-confirmed", false));
        Set<String> alumniResult = titles(repository.findAll(VisibilitySpecifications.visibleTo(
                context(1, null, null, null, true, Set.of()), mapping()
        )));
        assertEquals(Set.of("alumni-confirmed"), alumniResult);
    }

    @Test
    void visibilityComposesWithSearchDeletedFilterSortingAndPagination() {
        persist(
                item(2, null, null, "visible Cedar", false),
                item(2, null, null, "visible Maple", false),
                item(2, "COURSE", "999", "z hidden Maple", false),
                item(2, null, null, "visible Oak", true),
                item(2, null, null, "unrelated Pine", false)
        );
        Specification<VisibilityQueryTestItem> composed = VisibilitySpecifications
                .<VisibilityQueryTestItem>visibleTo(context(1, 100, 10, 2, false, Set.of()), mapping())
                .and((root, query, builder) -> builder.isFalse(root.get("deleted")))
                .and((root, query, builder) -> builder.like(root.get("title"), "%Maple%"));

        Page<VisibilityQueryTestItem> page = repository.findAll(
                composed,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "title"))
        );

        assertEquals(1, page.getTotalElements());
        assertEquals(1, page.getContent().size());
        assertEquals("visible Maple", page.getContent().get(0).getTitle());
        assertFalse(page.getContent().get(0).isDeleted());
    }

    @Test
    void supportsBothNullAndExplicitPublicStorageConventions() {
        persist(
                item(2, null, null, "null-public", false),
                item(2, "PUBLIC", null, "explicit-public", false),
                item(2, "PUBLIC", "unexpected", "malformed-public", false)
        );
        VisibilityQueryMapping<VisibilityQueryTestItem> explicitPublicMapping =
                new VisibilityQueryMapping<>(
                        root -> root.get("ownerId"),
                        root -> root.get("visibilityType"),
                        root -> root.get("visibilityValue"),
                        VisibilityScope::name,
                        "PUBLIC"
                );

        Set<String> nullConvention = titles(repository.findAll(
                VisibilitySpecifications.visibleTo(context(1, null, null, null, null, Set.of()), mapping())
        ));
        Set<String> explicitConvention = titles(repository.findAll(
                VisibilitySpecifications.visibleTo(
                        context(1, null, null, null, null, Set.of()), explicitPublicMapping
                )
        ));

        assertEquals(Set.of("null-public"), nullConvention);
        assertEquals(Set.of("explicit-public"), explicitConvention);
    }

    @Test
    void customPostCreatorBypassesAudienceButNotCustomStandaloneOrSoftDeleteRequirements() {
        persistPosts(
                post(1, "COURSE", "999", null, "creator-own"),
                post(2, "COURSE", "999", null, "ineligible-viewer"),
                post(1, "COURSE", "999", java.time.LocalDateTime.now(), "deleted-own"),
                post(1, "COURSE", "999", null, "event-own", 55),
                post(1, null, null, null, "general-own")
        );

        Set<String> creatorResult = postTitles(postRepository.findAll(
                PostVisibilityQuerySpecifications.visibleCustomPostsTo(
                        context(1, null, null, null, false, Set.of())
                )
        ));
        Set<String> ineligibleViewerResult = postTitles(postRepository.findAll(
                PostVisibilityQuerySpecifications.visibleCustomPostsTo(
                        context(3, null, null, null, false, Set.of())
                )
        ));

        assertEquals(Set.of("creator-own"), creatorResult);
        assertTrue(ineligibleViewerResult.isEmpty());
    }

    private static VisibilityQueryMapping<VisibilityQueryTestItem> mapping() {
        return new VisibilityQueryMapping<>(
                root -> root.get("ownerId"),
                root -> root.get("visibilityType"),
                root -> root.get("visibilityValue"),
                VisibilityScope::name,
                null
        );
    }

    private static VisibilityContext context(Integer viewerId, Integer courseId, Integer hostelId,
                                             Integer year, Boolean alumni, Set<Integer> clubs) {
        return new VisibilityContext(viewerId, courseId, hostelId, year, alumni, clubs);
    }

    private static VisibilityQueryTestItem item(Integer ownerId, String type, String value,
                                                String title, boolean deleted) {
        return new VisibilityQueryTestItem(ownerId, type, value, title, deleted);
    }

    private static Set<String> titles(List<VisibilityQueryTestItem> items) {
        return items.stream().map(VisibilityQueryTestItem::getTitle).collect(java.util.stream.Collectors.toSet());
    }

    private static void persist(VisibilityQueryTestItem... items) {
        entityManager.getTransaction().begin();
        for (VisibilityQueryTestItem item : items) {
            entityManager.persist(item);
        }
        entityManager.getTransaction().commit();
        entityManager.clear();
    }

    private static Post post(Integer creatorId, String visibilityType, String visibilityValue,
                             java.time.LocalDateTime deletedAt, String title) {
        return post(creatorId, visibilityType, visibilityValue, deletedAt, title, null);
    }

    private static Post post(Integer creatorId, String visibilityType, String visibilityValue,
                             java.time.LocalDateTime deletedAt, String title, Integer eventId) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        return Post.builder()
                .creator(entityManager.getReference(User.class, creatorId))
                .eventId(eventId)
                .title(title)
                .contentRaw(title)
                .visibilityType(visibilityType)
                .visibilityValue(visibilityValue)
                .allowComments(true)
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(deletedAt)
                .build();
    }

    private static void persistPosts(Post... posts) {
        entityManager.getTransaction().begin();
        for (Post post : posts) {
            entityManager.persist(post);
        }
        entityManager.getTransaction().commit();
        entityManager.clear();
    }

    private static Set<String> postTitles(List<Post> posts) {
        return posts.stream().map(Post::getTitle).collect(java.util.stream.Collectors.toSet());
    }

    private static void seedUsersAndRelationships() {
        entityManager.getTransaction().begin();
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            User user = User.builder()
                    .username("user" + i)
                    .email("user" + i + "@example.test")
                    .passwordHash("not-a-real-password")
                    .build();
            entityManager.persist(user);
            users.add(user);
        }
        entityManager.flush();
        entityManager.persist(connection(users.get(1), users.get(0), ConnectionStatus.CONNECTED));
        entityManager.persist(connection(users.get(0), users.get(2), ConnectionStatus.CONNECTED));
        entityManager.persist(connection(users.get(3), users.get(0), ConnectionStatus.PENDING));
        entityManager.getTransaction().commit();
        entityManager.clear();
    }

    private static UserConnection connection(User sender, User receiver, ConnectionStatus status) {
        return UserConnection.builder()
                .id(new UserConnectionId(sender.getId(), receiver.getId()))
                .sender(sender)
                .receiver(receiver)
                .status(status)
                .build();
    }
}
