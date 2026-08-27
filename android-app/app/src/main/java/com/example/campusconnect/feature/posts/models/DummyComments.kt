package com.example.campusconnect.feature.posts.models

val dummyComments = listOf(

    Comment(

        id = 1,

        postId = 1,

        parentCommentId = null,

        username = "aman_kumar",

        body = "I have last year's notes.",

        createdAt = "2h"
    ),

    Comment(

        id = 2,

        postId = 1,

        parentCommentId = 1,

        username = "vivek_verma",

        body = "Can you send them to me?",

        createdAt = "1h"
    ),

    Comment(

        id = 3,

        postId = 1,

        parentCommentId = null,

        username = "nikhil",

        body = "Check the shared drive folder.",

        createdAt = "30m"
    ),

    Comment(

        id = 4,

        postId = 2,

        parentCommentId = null,

        username = "khushi",

        body = "Amazing photo.",

        createdAt = "15m"
    )
)