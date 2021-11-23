# Room identity hash bug
## Description
When Room opens an existing database, it checks the identity hash stored in the existing database and the identity hash of the schema Room is trying to upgrade the database with. If the hashes are different and the database version wasn't updated, Room considers the situation to be an error since it cannot verify the data integrity.

According to the implementation (`RoomOpenHelper.java`), there are actually *two* hashes the existing database's hash is compared with. As it's said in a comment inside the class, this is due to fixes with the identity hash being inconsisted if field were reordered. This issue existed in Room of version 1 and was fixed shortly after, but in order to sustain backward compatibility, the old hash couldn't be deleted so a new field was introduced.

It is still required to check the schema hash against both new and legacy hashes because there might be a situation when a user has been using an app without cleaning the local storage since the app's version that used Room of version 1. If this is true, then the hash that is transferred from database to database with each database upgrade is equal to the **legacy hash** provided that the database schema remains unchanged.
## Bug
### Prerequisites
An app with the following setup:
- Kotlin plugin `1.4.31` (`org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.31`)
- Room version `2.2.6`
- An Room's entity defined in a particular way:
````kotlin
@Entity(tableName = "feed_entity")
data class FeedEntity(
    @ColumnInfo(name = "name")
    val name: String
) {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0                 // Note that the id field is defined inside the body of the entity
}
````

When the app is built, take a look at the generated source file for the Room database. There, find the place where `identityHash` and `legacyHash` are written. In my case, the values were the following:

| Field name | Hash |
| ---------- | ----- |
| `identityHash` | 9a8792da20d5fc06ca562ccc9b118cc6 |
| `legacyHash` | 8df0cc88464e07e64cf3a80503685ddd |

### How to reproduce
Perform the following steps:
- Update the kotlin plugin to the version `1.5.0` (`org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.0`)
- Update the Room's version to `2.3.0`

Note that the schema wasn't changed!

Try to build the app now and go to the generated source file of the Room database. There, once again find the place where the hashes are written. In my case, the hashes were the following:

| Field name | Hash |
| ---------- | ----- |
| `identityHash` | 9a8792da20d5fc06ca562ccc9b118cc6 |
| `legacyHash` | 4d32e475b12cf85dc092715855f4705a |

Note that while the `identityHash` wasn't changed (which is logical since the database schema wasn't changed at all), the `legacyHash` is now different. The comment in the `RoomOpenHelper.java` class above the `legacyHash` field says the following: 

>    /**
>     * Room v1 had a bug where the hash was not consistent if fields are reordered.
>     * The new has fixes it but we still need to accept the legacy hash.
>     */

However, as you've already seen, not only haven't we changed the schema of the database, but also we haven't altered the entity class!

Remembering the fact that the `legacyHash` might still be important under some condition, a simple library update will lead to crashes. If the old hash was `8df0cc88464e07e64cf3a80503685ddd` (the value of the old `legacyHash`), now it is checked against `identityHash` (`9a8792da20d5fc06ca562ccc9b118cc6`) and ** the new `legacyHash` ** (`4d32e475b12cf85dc092715855f4705a`). Obviously, both of these checks return `false` and given the fact that the version of the database remained the same, `IllegalStateException` is thrown. 

# Tl;dr
A simple library update under specific circumstances may lead to unexpected exceptions being thrown by Room