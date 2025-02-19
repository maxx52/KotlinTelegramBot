package ru.maxx52

import java.io.File

fun main() {
    val wordsFile = File("words.txt")

    if (!wordsFile.exists()) {
        println("Файл не найден: ${wordsFile.absolutePath}")
        return
    }

    val dictionary: MutableList<Word> = mutableListOf()

    val lines: List<String> = wordsFile.readLines()
    for (line in lines) {
        val parts = line.split("|")

        if (parts.size >= 2) {
            val original = parts[0].trim()
            val translate = parts[1].trim()
            val correctAnswersCount = parts.getOrNull(2)?.trim()?.toIntOrNull() ?: 0
            val word = Word(original, translate, correctAnswersCount)
            dictionary.add(word)
        } else {
            println("Пропуск строки: '$line' (неправильный формат)")
        }
    }

    dictionary.forEachIndexed { index, word ->
        println("${index + 1}. $word")
    }
}