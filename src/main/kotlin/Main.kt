package ru.maxx52

import java.io.File
import java.lang.NumberFormatException

fun main() {
    val dictionary: MutableList<Word> = mutableListOf()
    val notLearnedList: MutableList<Word> = mutableListOf()

    loadDictionary(dictionary)

    notLearnedList.addAll(dictionary)

    while (true) {
        println("Меню:")
        println("1 - Учить слова")
        println("2 - Статистика")
        println("0 - Выход")

        try {
            val inputMenu = readln().toInt()
            when (inputMenu) {
                1 -> {
                    println("Выбран пункт меню \"Учить слова\"")
                    if (notLearnedList.isEmpty()) {
                        println("Все слова в словаре выучены")
                        continue
                    } else {
                        val questionWords = notLearnedList.shuffled().take(4)
                        questionWords.forEachIndexed { index, word ->
                            println("${index + 1} - ${word.translate}")
                        }

                        val answerInput = readln().toIntOrNull()
                    }
                }
                2 -> {
                    println("Выбран пункт меню \"Статистика\"")
                    statistics(dictionary)
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

fun loadDictionary(dictionary: MutableList<Word>): Int {
    val wordsFile = File("words.txt")

    if (!wordsFile.exists()) {
        println("Файл не найден: ${wordsFile.absolutePath}")
        return 0
    }

    var correctAnswersCount = 0
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
    return correctAnswersCount
}

fun statistics(dictionary: List<Word>) {
    val totalCount = dictionary.size
    val learnedCount = dictionary.filter { it.correctAnswersCount >= 3 }

    if (totalCount == 0) {
        println("Словарь пуст.")
    } else {
        dictionary.forEachIndexed { index, word ->
            println("${index + 1}. $word")
        }
        val percent = (learnedCount.size.toDouble() / totalCount * 100).toInt()
        println("Выучено ${learnedCount.size} из $totalCount слов | $percent%")
    }
}