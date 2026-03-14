# CampusConnect Database

## Overview

The **CampusConnect database** supports a campus social and navigation application where students can interact, share posts, discover events, and view locations on the campus map.

The database is designed using **relational database principles** and implemented in **MySQL**.
It supports features such as:

* User registration and authentication
* Real-time user location tracking
* Social posting and commenting
* Event creation and participation
* Friendship management
* Campus location mapping
* User notifications

---

# Database Tables

## 1. users

Stores registered users of the system.

**Fields**

* id (Primary Key)
* username
* email (Unique)
* password_hash
* created_at

Purpose:
Identifies every user and acts as the central entity connected to many other tables.

---

## 2. user_presence

Stores the latest location of each user on the campus map.

**Fields**

* user_id (Primary Key, Foreign Key → users.id)
* latitude
* longitude
* last_update

Purpose:
Allows the application to display live student locations.

---

## 3. posts

Stores posts created by users.

**Fields**

* id (Primary Key)
* user_id (Foreign Key → users.id)
* content
* created_at

Purpose:
Represents the campus social feed.

---

## 4. comments

Stores comments made on posts.

**Fields**

* id (Primary Key)
* post_id (Foreign Key → posts.id)
* user_id (Foreign Key → users.id)
* content
* created_at

Purpose:
Allows users to interact with posts.

---

## 5. upvote

Stores upvotes given to posts.

**Fields**

* user_id (Primary Key, Foreign Key → users.id)
* post_id (Primary Key, Foreign Key → posts.id)
* created_at

Purpose:
Tracks which users liked which posts.

---

## 6. events

Stores campus events.

**Fields**

* id (Primary Key)
* title
* description
* poi_id (Foreign Key → poi.id)
* event_time
* created_by (Foreign Key → users.id)
* created_at

Purpose:
Represents events happening at specific campus locations.

---

## 7. event_participants

Stores users participating in events.

**Fields**

* user_id (Primary Key, Foreign Key → users.id)
* event_id (Primary Key, Foreign Key → events.id)
* joined_at

Purpose:
Tracks attendance of events.

---

## 8. friendships

Stores friendship relationships between users.

**Fields**

* user_id (Primary Key, Foreign Key → users.id)
* friend_id (Primary Key, Foreign Key → users.id)
* status
* created_at

Purpose:
Allows users to connect with each other.

---

## 9. poi (Points of Interest)

Stores campus locations.

**Fields**

* id (Primary Key)
* name
* latitude
* longitude
* category

Examples include:

* Library
* Hostels
* Sports Complex
* Academic Buildings

Purpose:
Used by the campus map feature.

---

## 10. notifications

Stores notifications for users.

**Fields**

* id (Primary Key)
* user_id (Foreign Key → users.id)
* type
* message
* is_read
* created_at

Purpose:
Notifies users about events such as likes, comments, or friend requests.

---

# Relationships

Key relationships in the database:

* users → posts (One-to-Many)
* users → comments (One-to-Many)
* users → user_presence (One-to-One)
* posts → comments (One-to-Many)
* posts ↔ upvote (Many-to-Many)
* users ↔ events (Many-to-Many through event_participants)
* users ↔ users (Friendships self-relationship)
* poi → events (One-to-Many)
* users → notifications (One-to-Many)

---

# Database Setup

To initialize the database:

1. Run `schema.sql` to create all tables.
2. Run `seed_pois.sql` to insert campus locations.
3. Run `demo_data.sql` to add sample data.
4. Use `queries_reference.sql` as reference for backend JDBC queries.

---

# File Structure

database/

schema.sql
seed_pois.sql
demo_data.sql
queries_reference.sql
ER_Diagram.png
database_readme.md

---

# Conclusion

The CampusConnect database is designed to support a **campus-wide social networking and event discovery system**.
It integrates **user interaction, event management, and geographic campus data** within a structured relational database suitable for backend integration using **Java JDBC**.
