# Vocaby on Android
![Tests](https://github.com/Vocaby/vocaby-android/actions/workflows/android_build.yml/badge.svg)

## Terms
**Entry**: A entry or a phrase in the dictionary. \
**Entry Group**: A collection of definitions that share the same type/part of speech for a given entry.

## MVP (v1.0.0)
1. User can look up an entry in the Vocaby dictionary.
    - An entry could have a pronunciation.
    - An entry must have a definition.
    - A definition could have an example.
    - A definition must be part of an entry group.
2. Looked up entries are saved in the search history.
3. User could look up an entry.
    - The dictionary definitions could be updated with each subsequent updates from the API
5. User can save entries (Local).
6. User can create a collection to group saved entries.
7. User can create a custom type.
8. User can view their saved entries.
    - User can delete their saves.
9. User can create a new entry in the dictionary (Local).
10. User can create a widget in the home screen to review saved definitions.
    - The widget will refresh periodically, changing the entry to review.
    - The user can refresh the widget manually.
11. User can turn on notifications to be notified of their saved entries for review.
    - User can change the notification refresh frequency.
    - User can change which save collection to review.
12. User can view Word of the Day (WoD) on the main screen (Fetched from the Vocaby API).
13. User can view a support page
    - User can read the FAQ
    - User can send a feedback
14. User can export/import saves, collections and entries
15. User can see statistics on their search history

**User-defined definitions will always be prioritized over Vocaby definitions.**

## Extra Features for Future Updates
- Quizzes / Games
- Notes
- Multiple custom dictionaries
- Shareable custom dictionary
- Book Scanning
- Save data in the cloud

_More to come..._
