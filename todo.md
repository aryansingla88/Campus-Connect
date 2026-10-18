# Posts Feature Review TODO

- [ ] Implement the Events feed. It currently shows “Events Feed Coming Soon,” and the backend only returns general posts.
- [ ] Decide whether deleted posts should show a “Post deleted” card. They currently disappear from the feed and cannot be opened in details; if the card is required, add the needed API data and UI.
- [ ] Prevent saving a post edit before tags finish loading, so the update cannot be sent with an empty tag list.
- [ ] Make “Edited” appear reliably, including when a post is edited soon after it was created.
- [ ] Show a comment-loading error when loading fails instead of displaying “No comments yet.”
- [ ] Add or update posts seed SQL to match the current schema. The available `demo_data.sql` uses old column names.
- [ ] Remove the unused temporary image copy from the create-post screen; the upload already uses the selected URI.
- [ ] Reduce posts-feature clutter: simplify the post-detail screen, remove stale comments and duplicate imports, and use one comment/reply data format.
