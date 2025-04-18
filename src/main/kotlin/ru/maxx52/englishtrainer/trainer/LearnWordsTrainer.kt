package ru.maxx52.englishtrainer.trainer

import ru.maxx52.englishtrainer.trainer.model.Question
import ru.maxx52.englishtrainer.trainer.model.Statistics
import ru.maxx52.englishtrainer.trainer.model.Word
import java.io.File

class LearnWordsTrainer(
    private val fileName: String = "words.txt",
    private val learnedAnswerCount: Int = 3,
    private val countOfQuestionWords: Int = 4,
) {
    private val dictionary = loadDictionary()
    var currentQuestion: Question? = null
        private set

    fun saveDictionary() {
        val wordsFile = File(fileName)
        wordsFile.printWriter().use { out ->
            for (word in dictionary) {
                out.println("${word.questionWord}|${word.translate}|${word.correctAnswersCount}")
            }
        }
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

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return currentQuestion?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer) + 1

            if (correctAnswerId == userAnswerIndex) {
                it.correctAnswer.incrementCorrectCount()
                saveDictionary()
                true
            } else {
                false
            }
        } ?: false
    }

    fun getCurrentQuestion(): Word? {
        return currentQuestion?.correctAnswer
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

    fun restartLearning() {
        val wordsFile = File(fileName)
        return try {
            wordsFile.printWriter().use { out ->
                for (word in dictionary) {
                    out.println("${word.questionWord}|${word.translate}|0")
                }
            }
            println("Прогресс успешно обновлён")
        } catch (e: Exception) {
            println("Ошибка при перезаписи словаря: ${e.message}")
        }
    }
}