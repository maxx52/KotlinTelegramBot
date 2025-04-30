package ru.maxx52.englishtrainer.data

import ru.maxx52.englishtrainer.trainer.IUserDictionary
import ru.maxx52.englishtrainer.trainer.model.Word
import java.sql.DriverManager
import java.sql.PreparedStatement

const val DB_URL = "jdbc:sqlite:words.db"
const val DEFAULT_LEARNING_WORDS = 3

class DatabaseUserDictionary : IUserDictionary {
    override fun getNumOfLearnedWords(userId: Long): Int {
        DriverManager.getConnection(DB_URL).use { connection ->
            val query = """
            SELECT COUNT(*) AS learnedCount 
            FROM user_answers
            WHERE user_id = ? AND correct_answer_count = 3
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
        DriverManager.getConnection(DB_URL).use { connection ->
            val query = "SELECT COUNT(*) AS size FROM words"
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery(query)

            return if (resultSet.next()) {
                resultSet.getInt("size")
            } else {
                0
            }.also {
                resultSet.close()
                statement.close()
            }
        }
    }

    override fun getLearnedWords(userId: Long): List<Word> {
        val learnedWords = mutableListOf<Word>()
        val dbUrl = "jdbc:sqlite:words.db"
        DriverManager.getConnection(dbUrl).use { connection ->
            val query = """
            SELECT w.id, w.text, w.translate 
            FROM user_answers ua
            JOIN words w ON ua.word_id = w.id
            WHERE ua.user_id = ? AND ua.correct_answer_count = 3
        """.trimIndent()

            val statement = connection.prepareStatement(query)
            statement.setLong(1, userId)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                val original = resultSet.getString("text").trim()
                val translate = resultSet.getString("translate").trim()
                val word = Word(original, translate, DEFAULT_LEARNING_WORDS)
                learnedWords.add(word)
            }
            resultSet.close()
            statement.close()
        }
        return learnedWords
    }

    override fun getUnlearnedWords(userId: Long): List<Word> {
        val unlearnedWords = mutableListOf<Word>()

        DriverManager.getConnection(DB_URL).use { connection ->
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
                val word = Word(original, translate, DEFAULT_LEARNING_WORDS)
                unlearnedWords.add(word)
            }
            resultSet.close()
            statement.close()
        }
        return unlearnedWords
    }

    override fun setCorrectAnswersCount(userId: Long, word: String, correctAnswersCount: Int) {
        DriverManager.getConnection(DB_URL).use { connection ->
            val checkStatement = connection.prepareStatement("""
            SELECT correct_answer_count 
            FROM user_answers 
            WHERE user_id = ? AND word_id = (SELECT id FROM words WHERE text = ?)
        """.trimIndent())
            checkStatement.setLong(1, userId)
            checkStatement.setString(2, word)
            val checkResultSet = checkStatement.executeQuery()

            if (checkResultSet.next()) {
                val currentCount = checkResultSet.getInt("correct_answer_count")

                val newCount = if (correctAnswersCount != 0) {
                    if (currentCount < 3) currentCount + 1 else currentCount
                } else {
                    currentCount
                }
                val updateStatement = connection.prepareStatement("""
                    UPDATE user_answers 
                    SET correct_answer_count = ? 
                    WHERE user_id = ? AND word_id = (SELECT id FROM words WHERE text = ?)
                """.trimIndent())
                updateStatement.setInt(1, newCount)
                updateStatement.setLong(2, userId)
                updateStatement.setString(3, word)
                val rows = updateStatement.executeUpdate()
                if (rows > 0) {
                    println("Количество правильных ответов для слова '$word' обновлено до $newCount.")
                } else {
                    println("Не удалось обновить записи для слова '$word' для пользователя с ID $userId.")
                }
                updateStatement.close()
            } else {
                val wordIdStatement = connection.prepareStatement("""
                    SELECT id FROM words WHERE text = ?""")
                wordIdStatement.setString(1, word)
                val wordIdResultSet = wordIdStatement.executeQuery()
                if (wordIdResultSet.next()) {
                    val wordId = wordIdResultSet.getLong("id")
                    val newCountToInsert: Int = (if (correctAnswersCount != 0) 1 else null)!!
                    val insertStatement = connection.prepareStatement("""
                    INSERT INTO user_answers (user_id, word_id, correct_answer_count) 
                    VALUES (?, ?, ?)
                """.trimIndent())
                    insertStatement.setLong(1, userId)
                    insertStatement.setLong(2, wordId)
                    insertStatement.setInt(3, newCountToInsert)
                    insertStatement.executeUpdate()
                    println("Новая запись создана для слова '$word' с количеством правильных ответов: $newCountToInsert.")
                    insertStatement.close()
                } else {
                    println("Слово '$word' не найдено в таблице слов.")
                }
                wordIdResultSet.close()
                wordIdStatement.close()
            }
            checkResultSet.close()
            checkStatement.close()
        }
    }

    override fun restartLearning(userId: Long) {
        DriverManager.getConnection(DB_URL).use { connection ->
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
            updateStatement.close()
        }
    }
}