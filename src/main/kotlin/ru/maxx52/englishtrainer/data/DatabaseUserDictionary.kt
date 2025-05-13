package ru.maxx52.englishtrainer.data

import ru.maxx52.englishtrainer.trainer.IUserDictionary
import ru.maxx52.englishtrainer.trainer.model.Word
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement

const val DB_URL = "jdbc:sqlite:words.db"

class DatabaseUserDictionary : IUserDictionary, AutoCloseable {
    private val connection: Connection = DriverManager.getConnection(DB_URL)

    override fun close() {
        connection.close()
    }

    override fun getNumOfLearnedWords(userId: Long): Int {
        connection.use { connection ->
            val query = """
            SELECT COUNT(*) AS learnedCount 
            FROM user_answers
            WHERE user_id = ? AND correct_answer_count >= 3
        """.trimIndent()

            val statement = connection.prepareStatement(query)
            statement.setLong(1, userId)
            val resultSet = statement.executeQuery()
            return if (resultSet.next()) {
                resultSet.getInt("learnedCount")
            } else {
                0
            }
        }
    }

    override fun getSize(): Int {
        val query = "SELECT COUNT(*) FROM words"
        connection.createStatement().use { stmt ->
            stmt.executeQuery(query).use { rs ->
                return if (rs.next()) rs.getInt(1) else 0
            }
        }
    }

    override fun getLearnedWords(userId: Long): List<Word> {
        val learnedWords = mutableListOf<Word>()
        connection.use { connection ->
            val query = """
            SELECT w.id, w.text, w.translate 
            FROM user_answers ua
            JOIN words w ON ua.word_id = w.id
            WHERE ua.user_id = ? AND ua.correct_answer_count >= 3
        """.trimIndent()

            val statement = connection.prepareStatement(query)
            statement.setLong(1, userId)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                val original = resultSet.getString("text").trim()
                val translate = resultSet.getString("translate").trim()
                val correctAnswerCount = resultSet.getInt("correct_answer_count")
                val word = Word(original, translate, correctAnswerCount)
                learnedWords.add(word)
            }
            close()
        }
        return learnedWords
    }

    override fun getUnlearnedWords(userId: Long): List<Word> {
        val unlearnedWords = mutableListOf<Word>()

        connection.use { connection ->
            val query = """
            SELECT w.id, w.text, w.translate, ua.correct_answer_count
            FROM words w
            LEFT JOIN user_answers ua ON ua.word_id = w.id
            WHERE (ua.user_id = ? AND ua.correct_answer_count < 3) OR ua.correct_answer_count is NULL
        """.trimIndent()

            val statement = connection.prepareStatement(query)
            statement.setLong(1, userId)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                val original = resultSet.getString("text").trim()
                val translate = resultSet.getString("translate").trim()
                val word = Word(original, translate, 4)
                unlearnedWords.add(word)
            }
            resultSet.close()
            statement.close()
        }
        return unlearnedWords
    }

    override fun setCorrectAnswersCount(userId: Long, word: String, correctAnswersCount: Int) {
        connection.use { connection ->
            val sql = """INSERT INTO user_answers (user_id, word_id, correct_answer_count)
                VALUES (?, (SELECT id FROM words WHERE text = ?), ?)
                ON CONFLICT(user_id, word_id) DO UPDATE SET correct_answer_count = ?
                """.trimIndent()

            connection.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, userId)
                stmt.setString(2, word)
                stmt.setInt(3, correctAnswersCount)
                stmt.setInt(4, correctAnswersCount)
                val rows = stmt.executeUpdate()
                if (rows > 0) {
                    println("Обновлено/вставлено: '$word' -> корректный ответ: $correctAnswersCount")
                } else {
                    println("Не удалось обновить или вставить запись для слова '$word'")
                }
            }
        }
    }

    override fun restartLearning(userId: Long) {
        connection.use { connection ->
            val updateStatement: PreparedStatement = connection.prepareStatement(
                "UPDATE user_answers SET correct_answer_count = 0 WHERE user_id = ?"
            )
            updateStatement.setLong(1, userId)
            val rowsAffected = updateStatement.executeUpdate()
            if (rowsAffected > 0) {
                println("Прогресс обучения для пользователя с ID $userId успешно сброшен.")
            } else {
                println("Не найдено записей для пользователя с ID $userId.")
            }
            close()
        }
    }
}