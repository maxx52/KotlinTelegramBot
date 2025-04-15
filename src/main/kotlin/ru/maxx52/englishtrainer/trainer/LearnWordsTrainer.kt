package ru.maxx52.englishtrainer.trainer

import ru.maxx52.englishtrainer.trainer.model.Question
import ru.maxx52.englishtrainer.trainer.model.Statistics
import ru.maxx52.englishtrainer.trainer.model.Word
import java.io.File

class LearnWordsTrainer(
    private val learnedAnswerCount: Int = 3,
) {
    private val dictionary = loadDictionary()
    var currentQuestion: Question? = null
        private set

    fun saveDictionary(words: List<Word>) {
        val wordsFile = File("words.txt")
        wordsFile.printWriter().use { out ->
            for (word in words) {
                out.println("${word.questionWord}|${word.translate}|${word.correctAnswersCount}")
            }
        }
        println("Словарь успешно сохранён.")
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
            .shuffled()
        if (notLearnedList.isEmpty()) return null
        val correctWord = notLearnedList.random().questionWord
        val correctAnswer = correctWord.let { word ->
            dictionary.firstOrNull { it.questionWord == word }
        } ?: return null
        val variants = mutableListOf(correctAnswer)
        val remainingWords = dictionary.filter { it != correctAnswer }
        val additionalVariants = remainingWords.shuffled().take(learnedAnswerCount)

        variants.addAll(additionalVariants)
        variants.shuffle()

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
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }

    fun getCurrentQuestion(): Word? {
        return currentQuestion?.correctAnswer
    }

    private fun loadDictionary(): List<Word> {
        try {
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
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("некорректный файл")
        }
    }
}