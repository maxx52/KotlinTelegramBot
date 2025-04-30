package ru.maxx52.englishtrainer.data

import ru.maxx52.englishtrainer.trainer.IUserDictionary
import ru.maxx52.englishtrainer.trainer.model.Word
import java.io.File

const val DEFAULT_FILE_NAME = "words.txt"

class FileUserDictionary(
    private val fileName: String = DEFAULT_FILE_NAME,
    private val learnedAnswerCount: Int = DEFAULT_LEARNING_WORDS
) : IUserDictionary {

    private val dictionary = try {
        loadDictionary()
    } catch (_: Exception) {
        throw IllegalStateException("Некорректный файл!")
    }

    override fun getNumOfLearnedWords(userId: Long): Int =
        dictionary.count { it.correctAnswersCount >= learnedAnswerCount }

    override fun getSize(): Int = dictionary.size

    override fun getLearnedWords(userId: Long): List<Word> =
        dictionary.filter { it.correctAnswersCount >= learnedAnswerCount }

    override fun getUnlearnedWords(userId: Long): List<Word> =
        dictionary.filter { it.correctAnswersCount < learnedAnswerCount }

    override fun setCorrectAnswersCount(userId: Long, original: String, correctAnswersCount: Int) {
        dictionary.find { it.questionWord == original }?.correctAnswersCount = correctAnswersCount
        saveDictionary()
    }

    override fun restartLearning(userId: Long) {
        val wordsFile = File(fileName)
        return try {
            wordsFile.printWriter().use { out ->
                for (word in dictionary) {
                    out.println("${word.questionWord}|${word.translate}|0")
                }
            }
        } catch (e: Exception) {
            println("Ошибка при перезаписи словаря: ${e.message}")
        }
    }

    private fun loadDictionary(): List<Word> {
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

    private fun saveDictionary() {
        val file = File(fileName)
        val newFileContent = dictionary.map { "${it.questionWord}|${it.translate}|${it.correctAnswersCount}" }
        file.writeText(newFileContent.joinToString(separator = "\n"))
        println("Словарь успешно сохранён")
    }
}