package ru.maxx52.englishtrainer.data

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

fun main() {
    try {
        DriverManager.getConnection(DB_URL).use {
            createTables(it)
        }
    } catch (e: SQLException) {
        println("Ошибка при подключении к базе данных: ${e.message}")
    }
}

fun createTables(connection: Connection) {
    connection.createStatement()
        .use {
            it.executeUpdate(
                """
        CREATE TABLE IF NOT EXISTS 'words' (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            text VARCHAR NOT NULL,
            translate VARCHAR NOT NULL
        );
    """.trimIndent()
            )
        }

    connection.createStatement()
        .use {
            it.executeUpdate(
                """            
            CREATE TABLE IF NOT EXISTS user_answers (
            user_id INTEGER,
            word_id INTEGER,
            correct_answer_count INTEGER,
            updated_at TIMESTAMP,
            PRIMARY KEY (user_id, word_id));
        """.trimIndent()
            )
        }
    println("Таблицы успешно созданы.")

    val fileName = File("words.txt")
    updateDictionary(connection, fileName)
}

fun updateDictionary(connection: Connection, wordsFile: File) {
    connection
        .prepareStatement("INSERT INTO words (text, translate) VALUES (?, ?)")
        .use { statement ->
            wordsFile.forEachLine { line ->
                val parts = line.split("|")
                if (parts.size == 3) {
                    val original = parts[0].trim()
                    val translate = parts[1].trim()

                    statement.setString(1, original)
                    statement.setString(2, translate)
                    statement.addBatch()
                } else {
                    println("Пропуск строки: '$line' (неправильный формат)")
                }
            }
            statement.executeBatch()
        }
    println("Все слова загружены в базу данных.")
}