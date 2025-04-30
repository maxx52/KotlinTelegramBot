package ru.maxx52.englishtrainer.data

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException

fun main() {
    try {
        val connection: Connection = DriverManager.getConnection("jdbc:sqlite:words.db")
        createTables(connection)
        connection.close()
    } catch (e: SQLException) {
        println("Ошибка при подключении к базе данных: ${e.message}")
    }
}

fun createTables(connection: Connection) {
    val fileName = File("words.txt")
    connection.createStatement().executeUpdate("""
        CREATE TABLE IF NOT EXISTS 'users' (
          'id' integer PRIMARY KEY,
          'username' VARCHAR,
          'created_at' TIMESTAMP,
          'chat_id' INTEGER
        );
    """.trimIndent())

    connection.createStatement().executeUpdate("""
        CREATE TABLE IF NOT EXISTS 'words' (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            text VARCHAR NOT NULL,
            translate VARCHAR NOT NULL
        );
    """.trimIndent())

    connection.createStatement().executeUpdate("""
            CREATE TABLE IF NOT EXISTS 'user_answers' (
              'user_id' INTEGER,
              'word_id' INTEGER,
              'correct_answer_count' INTEGER,
              'updated_at' TIMESTAMP
            );
        """.trimIndent())
    println("Таблицы успешно созданы.")
    updateDictionary(fileName)
}

fun updateDictionary(wordsFile: File) {
    val dbUrl = "jdbc:sqlite:words.db"
    DriverManager.getConnection(dbUrl).use { connection ->
        val insertStatement: PreparedStatement = connection.prepareStatement(
            "INSERT INTO words (text, translate, correctAnswerCount) VALUES (?, ?, ?)"
        )

        wordsFile.forEachLine { line ->
            val parts = line.split("|")
            if (parts.size == 3) {
                val original = parts[0].trim()
                val translate = parts[1].trim()
                val correctAnswerCount = parts[2].trim().toIntOrNull() ?: 0

                insertStatement.setString(1, original)
                insertStatement.setString(2, translate)
                insertStatement.setInt(3, correctAnswerCount)
                insertStatement.addBatch()
            } else {
                println("Пропуск строки: '$line' (неправильный формат)")
            }
        }
        insertStatement.executeBatch()
        insertStatement.close()

        println("Все слова загружены в базу данных.")
    }
}