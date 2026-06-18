package com.example.campusconnect.feature.posts.models

val dummyPosts = listOf(

    Post(
        id = 1,
        username = "pratham_yadav",
        title = "Need DBMS Notes",
        body = "Can someone share normalization notes before the mid-sem exam?",
        tags = listOf(
            PostTag(1, "Study Help")
        ),
        imageUrl = null,
        upvotes = 42,
        downvotes = 3,
        createdAt = "2h"
    ),

    Post(
        id = 2,
        username = "aman_kumar",
        title = "Best DSA Playlist?",
        body = "Looking for a complete DSA playlist for placement preparation.",
        tags = listOf(
            PostTag(1, "Study Help")
        ),
        imageUrl = null,
        upvotes = 31,
        downvotes = 1,
        createdAt = "5h"
    ),

    Post(
        id = 3,
        username = "nikhil_patidar",
        title = "Microsoft OA Experience",
        body = "The OA had 2 coding questions and one was graph based. Happy to answer doubts.",
        tags = listOf(
            PostTag(2, "Placements")
        ),
        imageUrl = null,
        upvotes = 67,
        downvotes = 2,
        createdAt = "1d"
    ),

    Post(
        id = 4,
        username = "vivek_verma",
        title = "Internship Resources",
        body = "Sharing a drive containing OA sheets, interview experiences and resume templates.",
        tags = listOf(
            PostTag(2, "Placements")
        ),
        imageUrl = null,
        upvotes = 89,
        downvotes = 1,
        createdAt = "8h"
    ),

    Post(
        id = 5,
        username = "khushi_sharma",
        title = "Lost Wallet Near Library",
        body = "Brown leather wallet with college ID inside. Please contact if found.",
        tags = listOf(
            PostTag(3, "Lost & Found")
        ),
        imageUrl = null,
        upvotes = 56,
        downvotes = 0,
        createdAt = "3h"
    ),

    Post(
        id = 6,
        username = "aryan_singla",
        title = "Found AirPods",
        body = "Found AirPods near the Mechanical Department block.",
        tags = listOf(
            PostTag(3, "Lost & Found")
        ),
        imageUrl = null,
        upvotes = 38,
        downvotes = 0,
        createdAt = "6h"
    ),

    Post(
        id = 7,
        username = "arvind_kumar",
        title = "Hostel WiFi Down Again",
        body = "Hostel 6 WiFi has been down since morning. Is everyone facing the same issue?",
        tags = listOf(
            PostTag(4, "Hostels")
        ),
        imageUrl = null,
        upvotes = 74,
        downvotes = 4,
        createdAt = "4h"
    ),

    Post(
        id = 8,
        username = "lokesh_kumawat",
        title = "Room Available in Hostel",
        body = "One room is vacant after semester withdrawal. Contact if interested.",
        tags = listOf(
            PostTag(4, "Hostels")
        ),
        imageUrl = null,
        upvotes = 22,
        downvotes = 1,
        createdAt = "1d"
    ),

    Post(
        id = 9,
        username = "shivansh_sharma",
        title = "Mess Paneer Day Review",
        body = "Today was surprisingly decent. Paneer wasn't rubber this time.",
        tags = listOf(
            PostTag(5, "Mess & Food")
        ),
        imageUrl = null,
        upvotes = 63,
        downvotes = 5,
        createdAt = "7h"
    ),

    Post(
        id = 10,
        username = "sohel_paul",
        title = "Best Late Night Food?",
        body = "Where do you guys eat after 11 PM near campus?",
        tags = listOf(
            PostTag(5, "Mess & Food")
        ),
        imageUrl = null,
        upvotes = 45,
        downvotes = 2,
        createdAt = "9h"
    ),

    Post(
        id = 11,
        username = "shivam_gupta",
        title = "Night Canteen Open?",
        body = "Is the night canteen operating during exams?",
        tags = listOf(
            PostTag(6, "Night Life")
        ),
        imageUrl = null,
        upvotes = 28,
        downvotes = 0,
        createdAt = "3h"
    ),

    Post(
        id = 12,
        username = "vansh_gupta",
        title = "Anyone Up For Chai?",
        body = "Heading to Gokul after study hours. Anyone joining?",
        tags = listOf(
            PostTag(6, "Night Life")
        ),
        imageUrl = null,
        upvotes = 35,
        downvotes = 1,
        createdAt = "1h"
    ),

    Post(
        id = 13,
        username = "pratiksha_dubey",
        title = "Techspardha Schedule Released",
        body = "The official schedule has finally been released.",
        tags = listOf(
            PostTag(7, "Events")
        ),
        imageUrl = null,
        upvotes = 58,
        downvotes = 2,
        createdAt = "5h"
    ),

    Post(
        id = 14,
        username = "gopala_chachre",
        title = "Photography Workshop",
        body = "Photography club is organizing a workshop this weekend.",
        tags = listOf(
            PostTag(7, "Events")
        ),
        imageUrl = "https://picsum.photos/600/400",
        upvotes = 41,
        downvotes = 1,
        createdAt = "12h"
    ),

    Post(
        id = 15,
        username = "akshat_mahatha",
        title = "Football Team Selection",
        body = "Trials will be conducted next Monday evening.",
        tags = listOf(
            PostTag(8, "Sports")
        ),
        imageUrl = null,
        upvotes = 66,
        downvotes = 3,
        createdAt = "6h"
    ),

    Post(
        id = 16,
        username = "sanchit_tewari",
        title = "Badminton Partner Needed",
        body = "Looking for someone to play badminton regularly after classes.",
        tags = listOf(
            PostTag(8, "Sports")
        ),
        imageUrl = null,
        upvotes = 29,
        downvotes = 0,
        createdAt = "1d"
    ),

    Post(
        id = 17,
        username = "vanshika_malhotra",
        title = "Coding Club Recruitment",
        body = "Applications for the coding club are now open.",
        tags = listOf(
            PostTag(9, "Clubs")
        ),
        imageUrl = null,
        upvotes = 53,
        downvotes = 1,
        createdAt = "10h"
    ),

    Post(
        id = 18,
        username = "arijit_das",
        title = "Robotics Club Showcase",
        body = "We will be showcasing our latest rover prototype tomorrow.",
        tags = listOf(
            PostTag(9, "Clubs")
        ),
        imageUrl = "https://picsum.photos/600/401",
        upvotes = 72,
        downvotes = 2,
        createdAt = "8h"
    ),

    Post(
        id = 19,
        username = "abhishek_dhangar",
        title = "Selling Scientific Calculator",
        body = "Casio calculator in excellent condition. DM if interested.",
        tags = listOf(
            PostTag(10, "Buy & Sell")
        ),
        imageUrl = null,
        upvotes = 24,
        downvotes = 0,
        createdAt = "2d"
    ),

    Post(
        id = 20,
        username = "pratham_yadav",
        title = "Selling First Year Books",
        body = "Complete first year set available at a reasonable price.",
        tags = listOf(
            PostTag(10, "Buy & Sell")
        ),
        imageUrl = null,
        upvotes = 37,
        downvotes = 1,
        createdAt = "18h"
    )
)