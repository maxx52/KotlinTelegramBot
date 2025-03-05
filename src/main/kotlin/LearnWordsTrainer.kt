package ru.maxx52

import java.io.File

const val CORRECT_COUNTER = 3

data class Statistics(
    val totalCount: Int,
    val learnedCount: List<Word>,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer {
    private val dictionary = loadDictionary()

    private var question: Question? = null

    fun saveDictionary(words: List<Word>) {
        val wordsFile = File("words.txt")
        wordsFile.printWriter().use { out ->
            for (word in words) {
                out.println("${word.questionWord}|${word.translate}|${word.correctAnswersCount}")
            }
        }
        println("Словарь успешно сохранён.")
    }

    fun getStatistic() : Statistics {
        val totalCount = dictionary.size
        val learnedCount = dictionary.filter { it.correctAnswersCount >= CORRECT_COUNTER }
        val percent: Int = learnedCount.size / totalCount * 100
        return Statistics(totalCount, learnedCount, percent)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < CORRECT_COUNTER }
        if (notLearnedList.isEmpty()) return null
        question = Question(
            variants = notLearnedList.shuffled().take(COUNT_OF_WORDS),
            correctAnswer = notLearnedList.random()
        )
        return question
    }


    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)

            if (correctAnswerId == userAnswerIndex) {
                it.correctAnswer.incrementCorrectCount()
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): List<Word> {
        val dictionary = mutableListOf<Word>()
        val wordsFile = File("words.txt")

        if (!wordsFile.exists()) {
            println("Файл не найден: ${wordsFile.absolutePath}")
            return dictionary
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
    }
}