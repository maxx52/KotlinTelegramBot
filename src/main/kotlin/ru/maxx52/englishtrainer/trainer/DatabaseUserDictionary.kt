package ru.maxx52.englishtrainer.trainer

import ru.maxx52.englishtrainer.telegram.DEFAULT_LEARNING_WORDS
import ru.maxx52.englishtrainer.trainer.model.Question
import ru.maxx52.englishtrainer.trainer.model.Word
import java.sql.DriverManager
import java.sql.PreparedStatement

const val DB_URL = "jdbc:sqlite:words.db"

class DatabaseUserDictionary : IUserDictionary {
    override fun getNumOfLearnedWords(userId: Int): Int {
        DriverManager.getConnection(DB_URL).use { connection ->
            val query = """
            SELECT COUNT(*) AS learnedCount 
            FROM user_answers
            WHERE user_id = ? AND correct_answer_count = 3
        """.trimIndent()

            val statement = connection.prepareStatement(query)
            statement.setInt(1, userId)
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

    override fun getLearnedWords(userId: Int): List<Word> {
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
            statement.setInt(1, userId)
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

    override fun getUnlearnedWords(userId: Int): List<Word> {
        val unlearnedWords = mutableListOf<Word>()
        DriverManager.getConnection(DB_URL).use { connection ->
            val query = """
            SELECT w.id, w.text, w.translate 
            FROM user_answers ua
            JOIN words w ON ua.word_id = w.id
            WHERE ua.user_id = ? AND ua.correct_answer_count < 3
        """.trimIndent()

            val statement = connection.prepareStatement(query)
            statement.setInt(1, userId)
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

    override fun setCorrectAnswersCount(word: String, correctAnswersCount: Int) {
        DriverManager.getConnection(DB_URL).use { connection ->
            val updateStatement = connection.prepareStatement(
                "UPDATE words SET correctAnswerCount = ? WHERE text = ?"
            )

            updateStatement.setInt(1, correctAnswersCount)
            updateStatement.setString(2, word)
            val rowsAffected = updateStatement.executeUpdate()
            if (rowsAffected > 0) {
                println("Количество правильных ответов для слова '$word' успешно обновлено.")
            } else {
                println("Не найдено записи для слова '$word'.")
            }
            updateStatement.close()
        }
    }

    override fun restartLearning(userId: Int) {
        DriverManager.getConnection(DB_URL).use { connection ->
            val updateStatement: PreparedStatement = connection.prepareStatement(
                "UPDATE user_answers SET correct_answer_count = 0 WHERE user_id = ?"
            )
            updateStatement.setInt(1, userId)
            val rowsAffected = updateStatement.executeUpdate()
            if (rowsAffected > 0) {
                println("Прогресс обучения для пользователя с ID $userId успешно сброшен.")
            } else {
                println("Не найдено записей для пользователя с ID $userId.")
            }
            updateStatement.close()
        }
    }

    fun getNextQuestion(userId: Int, learnedAnswerCount: Int, countOfQuestionWords: Int): Question? {
        DriverManager.getConnection(DB_URL).use { connection ->
            val notLearnedList = mutableListOf<Word>()
            val notLearnedStatement = connection.prepareStatement("""
            SELECT w.id, w.text, w.translate 
            FROM user_answers ua
            JOIN words w ON ua.word_id = w.id
            WHERE ua.user_id = ? AND ua.correct_answer_count < ?
        """.trimIndent())

            notLearnedStatement.setInt(1, userId)
            notLearnedStatement.setInt(2, learnedAnswerCount)
            val notLearnedResultSet = notLearnedStatement.executeQuery()
            while (notLearnedResultSet.next()) {
                val original = notLearnedResultSet.getString("text").trim()
                val translate = notLearnedResultSet.getString("translate").trim()
                notLearnedList.add(Word(original, translate, correctAnswersCount = 0))
            }

            if (notLearnedList.isEmpty()) {
                notLearnedStatement.close()
                return null
            }

            var variants = notLearnedList.shuffled().take(countOfQuestionWords)
            val correctAnswer = variants.random()

            if (variants.size < countOfQuestionWords) {
                val learnedList = mutableListOf<Word>()
                val learnedStatement = connection.prepareStatement("""
                SELECT w.id, w.text, w.translate 
                FROM user_answers ua
                JOIN words w ON ua.word_id = w.id
                WHERE ua.user_id = ? AND ua.correct_answer_count >= ?
            """.trimIndent())

                learnedStatement.setInt(1, userId)
                learnedStatement.setInt(2, learnedAnswerCount)
                val learnedResultSet = learnedStatement.executeQuery()
                while (learnedResultSet.next()) {
                    val original = learnedResultSet.getString("text").trim()
                    val translate = learnedResultSet.getString("translate").trim()
                    learnedList.add(Word(original, translate, correctAnswersCount = 0))
                }
                variants = (variants + learnedList.shuffled().take(countOfQuestionWords - variants.size)).shuffled()
            }
            notLearnedStatement.close()
            return Question(variants, correctAnswer)
        }
    }
}