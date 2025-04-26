package ru.maxx52.englishtrainer.trainer

import ru.maxx52.englishtrainer.telegram.DEFAULT_LEARNING_WORDS
import ru.maxx52.englishtrainer.trainer.model.Question
import ru.maxx52.englishtrainer.trainer.model.Statistics
import ru.maxx52.englishtrainer.trainer.model.Word
import java.io.File
import java.sql.DriverManager
import java.sql.PreparedStatement

const val DEFAULT_FILE_NAME = "words.txt"

class FileUserDictionary(
    private val fileName: String = DEFAULT_FILE_NAME,
    private val learnedAnswerCount: Int = DEFAULT_LEARNING_WORDS,
    private val countOfQuestionWords: Int = 4,
) : IUserDictionary {

    private val dictionary = try {
        loadDictionary()
    } catch (_: Exception) {
        throw IllegalStateException("Некорректный файл!")
    }

    override fun getNumOfLearnedWords(userId: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getSize(): Int {
        TODO("Not yet implemented")
    }

    override fun getLearnedWords(userId: Int): List<Word> {
        TODO("Not yet implemented")
    }

    override fun getUnlearnedWords(userId: Int): List<Word> {
        TODO("Not yet implemented")
    }

    override fun setCorrectAnswersCount(original: String, correctAnswersCount: Int) {
        dictionary.find { it.original == original }?.correctAnswersCount = correctAnswersCount
        saveDictionary()
    }

    override fun restartLearning(userId: Int) {
        val dbUrl = "jdbc:sqlite:words.db"

        // Подключение к базе данных
        DriverManager.getConnection(dbUrl).use { connection ->
            // Подготовленный запрос для сброса правильных ответов для конкретного пользователя
            val updateStatement: PreparedStatement = connection.prepareStatement(
                "UPDATE user_answers SET correct_answer_count = 0 WHERE user_id = ?"
            )

            // Устанавливаем ID пользователя
            updateStatement.setInt(1, userId)

            // Выполняем обновление
            val rowsAffected = updateStatement.executeUpdate()
            if (rowsAffected > 0) {
                println("Прогресс обучения для пользователя с ID $userId успешно сброшен.")
            } else {
                println("Не найдено записей для пользователя с ID $userId.")
            }

            // Закрытие ресурсов
            updateStatement.close()
        }
    }

    fun loadDictionary(): List<Word> {
        try {
            val dictionary = mutableListOf<Word>()
            val wordsFile = File(fileName)

            if (!wordsFile.exists()) {
                File("words.txt").copyTo(wordsFile)
            }

            var correctAnswersCount: Int
            val lines: List<String> = wordsFile.readLines()
            for (line in lines) {
                val parts = line.split("|")
                if (parts.size >= 2) {
                    val original = parts[0].trim()
                    val translate = parts[1].trim()
                    correctAnswersCount = parts.getOrNull(2)?.trim()?.toIntOrNull() ?: 0
                    val word = Word(original, translate, correctAnswersCount)
                    dictionary.add(word)
                } else {
                    println("Пропуск строки: '$line' (неправильный формат)")
                }
            }
            return dictionary
        } catch (_: IndexOutOfBoundsException) {
            throw IllegalStateException("некорректный файл")
        }
    }

    fun saveDictionary() {
        val file = File(fileName)
        val newFileContent = dictionary.map { "${it.original}|${it.translate}|${it.correctAnswersCount}" }
        file.writeText(newFileContent.joinToString(separator = "\n"))
        println("Словарь успешно сохранён")
    }

    fun getStatistics() : Statistics {
        val totalCount = dictionary.size
        val learnedCount = dictionary
            .filter { it.correctAnswersCount >= learnedAnswerCount }
        val percent = learnedCount.size * 100 / totalCount
        return Statistics(totalCount, learnedCount, percent)
    }

    fun getNextQuestion(): Question? {
        var currentQuestion: Question
        val notLearnedList = dictionary
            .filter { it.correctAnswersCount < learnedAnswerCount }
        if (notLearnedList.isEmpty()) return null

        var variants = notLearnedList.take(countOfQuestionWords)
        val correctAnswer = variants.random()

        if (variants.size < countOfQuestionWords) {
            val learnedList = dictionary
                .filter { it.correctAnswersCount >= learnedAnswerCount }
                .shuffled()
            variants = (variants + learnedList.take(countOfQuestionWords - variants.size))
                .shuffled()
        }
        currentQuestion = Question(
            variants,
            correctAnswer = correctAnswer
        )
        return currentQuestion
    }
}