package com.campus.Campus_Connect.repository;

import com.campus.Campus_Connect.model.*;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Repository
public class PostRepository {

    private final DataSource dataSource;

    public PostRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // POSTS
    public List<Post> getAllPosts(int currentUserId) {

        List<Post> posts = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {

            // 🔥 DEBUG: check data exists
            System.out.println("=== DEBUG START ===");

            String debugQuery = "SELECT COUNT(*) FROM posts";
            PreparedStatement debugStmt = conn.prepareStatement(debugQuery);
            ResultSet debugRs = debugStmt.executeQuery();

            if (debugRs.next()) {
                System.out.println("POST COUNT FROM SPRING: " + debugRs.getInt(1));
            }

            System.out.println("=== DEBUG END ===");

            // ✅ MAIN QUERY (SAFE VERSION)
            String query = """
            SELECT p.id, p.content, p.created_at, u.username
            FROM posts p
            LEFT JOIN users u ON p.user_id = u.id
            ORDER BY p.created_at DESC
        """;

            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                int postId = rs.getInt("id");
                String content = rs.getString("content");
                Timestamp createdAt = rs.getTimestamp("created_at");

                String username = rs.getString("username");
                if (username == null) username = "unknown";

                // 🔥 DEBUG
                System.out.println("FOUND POST: " + postId);

                // ✅ FETCH EXTRA DATA
                int upvotes = getUpvoteCount(conn, postId);
                boolean hasUpvoted = checkUpvote(conn, postId, currentUserId);
                List<Comment> comments = getComments(conn, postId);

                Post post = new Post(
                        postId,
                        username,
                        content,
                        upvotes,
                        createdAt.toString(),
                        hasUpvoted,
                        comments
                );

                posts.add(post);
            }

            System.out.println("TOTAL POSTS FETCHED: " + posts.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return posts;
    }


    //UPVOTE COUNT
    private int getUpvoteCount(Connection conn, int postId) throws SQLException {

        String query = "SELECT COUNT(*) FROM upvote WHERE post_id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, postId);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getInt(1);
        }

        return 0;
    }


    // CHECK IF USER UPVOTED
    private boolean checkUpvote(Connection conn, int postId, int userId) throws SQLException {

        String query = "SELECT * FROM upvote WHERE post_id = ? AND user_id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);

        stmt.setInt(1, postId);
        stmt.setInt(2, userId);

        ResultSet rs = stmt.executeQuery();

        return rs.next(); // if exists → true
    }


    // GET COMMENTS
        private List<Comment> getComments(Connection conn, int postId) throws SQLException {

            List<Comment> comments = new ArrayList<>();

            String query = """
        SELECT c.id, c.content, c.created_at, u.username
        FROM comments c 
        JOIN users u ON c.user_id = u.id
        WHERE c.post_id = ?
        ORDER BY c.created_at ASC
    """;

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, postId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                Timestamp createdAt = rs.getTimestamp("created_at");

                Comment comment = new Comment(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("content"),
                        createdAt.toString()
                );

                comments.add(comment);
            }

            return comments;
        }

        public Post createPost(int userId, String content) {

            try (Connection conn = dataSource.getConnection()) {

                String query = "INSERT INTO posts (user_id, content, created_at) VALUES (?, ?, NOW())";

                PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

                stmt.setInt(1, userId);
                stmt.setString(2, content);

                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();

                if (rs.next()) {
                    int postId = rs.getInt(1);

                    Post post = new Post();
                    post.setId(postId);
                    post.setContent(content);
                    return post;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

    public void addComment(int postId, int userId, String content) {

        try (Connection conn = dataSource.getConnection()) {

            String query = "INSERT INTO comments (post_id, user_id, content, created_at) VALUES (?, ?, ?, NOW())";

            PreparedStatement stmt = conn.prepareStatement(query);

            // ✅ CORRECT ORDER
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            stmt.setString(3, content);

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggleUpvote(int postId, int userId) {

        try (Connection conn = dataSource.getConnection()) {

            String checkQuery = "SELECT 1 FROM upvote WHERE post_id = ? AND user_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);


            checkStmt.setInt(1, postId);
            checkStmt.setInt(2, userId);

            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {

                String deleteQuery = "DELETE FROM upvote WHERE post_id = ? AND user_id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);

                deleteStmt.setInt(1, postId);
                deleteStmt.setInt(2, userId);

                deleteStmt.executeUpdate();

            } else {

                String insertQuery = "INSERT INTO upvote (post_id, user_id) VALUES (?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);

                insertStmt.setInt(1, postId);
                insertStmt.setInt(2, userId);

                insertStmt.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}