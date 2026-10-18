# Posts UI follow-ups

Deferred updates for the Posts feature:

1. **Fix post image scaling**
   - Images in `PostCard` and `PostDetailCard` are currently cropped.
   - Revisit their sizing and scaling so images display appropriately in both the feed card and post detail view.

2. **Align topic tags consistently in `PostCard`**
   - The topic chip shifts position depending on whether the post has the owner-only three-dot menu.
   - Place the topic chip in a consistent row below the author and date metadata, independent of the menu.
   - Keep the owner menu at the trailing edge of the metadata row; reserve a consistent action slot so the header alignment does not shift between owned and other users' posts.
