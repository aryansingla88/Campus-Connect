package com.example.campusconnect.feature.posts.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/** Highlights each query word while preserving the original text and casing. */
fun highlightPostSearchMatches(text: String, query: String): AnnotatedString {
    val terms = query.trim().split(Regex("\\s+")).filter(String::isNotBlank).distinct()
    if (terms.isEmpty()) return AnnotatedString(text)

    val ranges = terms.flatMap { term ->
        Regex(Regex.escape(term), RegexOption.IGNORE_CASE)
            .findAll(text)
            .map { it.range }
            .toList()
    }.sortedBy { it.first }

    if (ranges.isEmpty()) return AnnotatedString(text)

    return buildAnnotatedString {
        var cursor = 0
        ranges.forEach { range ->
            val start = maxOf(range.first, cursor)
            val endExclusive = range.last + 1
            if (start >= endExclusive) return@forEach
            append(text.substring(cursor, start))
            withStyle(SpanStyle(background = Color(0xFFFFD54F), color = Color(0xFF171717))) {
                append(text.substring(start, endExclusive))
            }
            cursor = endExclusive
        }
        append(text.substring(cursor))
    }
}
