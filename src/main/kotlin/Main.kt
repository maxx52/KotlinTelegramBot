package ru.maxx52

import java.io.File
import java.lang.NumberFormatException

fun main() {
    val dictionary: MutableList<Word> = mutableListOf()

    while (true) {
        println("Меню:")
        println("1 - Учить слова")
        println("2 - Статистика")
        println("0 - Выход")

        try {
            val inputMenu = readln().toInt()
            when(inputMenu) {
                1 -> {
                    println("Выбран пункт меню \"Учить слова\"")
                }
                2 -> {
                    println("Выбран пункт меню \"Статистика\"")
                }
                0 -> {
                    println("Выход из программы")
                    return
                }
                else -> {
                    println("Введите число 1, 2 или 0")
                }
            }
        } catch (e: NumberFormatException) {
            println("Введите число 1, 2 или 0")
        }
    }
}

fun loadDictionary(dictionary: MutableList<Word>) {
    val wordsFile = File("words.txt")

    if (!wordsFile.exists()) {
        println("Файл не найден: ${wordsFile.absolutePath}")
        return
    }

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
}

fun statistics(dictionary: List<Word>) {
    if (dictionary.isEmpty()) {
        println("Словарь пуст.")
    } else {
        dictionary.forEachIndexed { index, word ->
            println("${index + 1}. $word")
        }
    }
}