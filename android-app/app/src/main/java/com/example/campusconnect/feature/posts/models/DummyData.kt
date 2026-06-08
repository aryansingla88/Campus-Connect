package com.example.campusconnect.feature.posts.models

val dummyPosts = listOf(

    Post(
        id = 1,
        username = "pratham_yadav",
        title = "Need DBMS Notes",
        body = "Can someone share normalization notes?",
        tag="study help",
        imageUrl = null,
        upvotes = 42,
        downvotes = 3,
        commentCount = 12,
        createdAt = "2h"
    ),

    Post(
        id = 2,
        username = "aman_kumar",
        title = "Campus Sunset",
        body = "Clicked this near hostel block.",
        tag="photography",
        imageUrl = "https://picsum.photos/600/400",
        upvotes = 91,
        downvotes = 5,
        commentCount = 24,
        createdAt = "5h"
    ),
    Post(
        tag="sports",
        id=3,
        username="Sumit Chauhan",
        title="Sports Week Coming",
        body="So, as the sports week is nearing, I and my friends feel excited for the upcoming events. " +
                "But the College Authorities seem to lack" +
                " even an ounce of zeal for the events and" +
                " no proactiveness seems to be reflected" +
                " in the way they are preparing" +
                " for hosting the events.",
        upvotes=23,
        downvotes=2,
        commentCount=12,
        createdAt="2d"
    )
)