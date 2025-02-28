package ru.maxx52

import java.io.File
import java.lang.NumberFormatException

fun main() {
    val dictionary: MutableList<Word> = mutableListOf()

    loadDictionary(dictionary)

    while (true) {
        println("Меню:")
        println("1 - Учить слова")
        println("2 - Статистика")
        println("0 - Выход")

        try {
            val inputMenu = getUserInput()
            when (inputMenu) {
                1 -> {
                    println("Выбран пункт меню \"Учить слова\"")

                    val notLearnedList: MutableList<Word> = mutableListOf()
                    notLearnedList.addAll(dictionary)

                    if (notLearnedList.isEmpty()) {
                        println("Все слова в словаре выучены")
                        continue
                    } else {
                        val questionWords = notLearnedList.shuffled().take(COUNT_OF_WORDS)
                        val correctAnswerIndex = questionWords.indices.random()
                        val correctWord = questionWords[correctAnswerIndex]

                        println("${correctWord.original}:")

                        questionWords.forEachIndexed { index, word ->
                            println("${index + 1} - ${word.translate}")
                        }

                        println("-------------")
                        println("0 - Меню")

                        val userAnswerInput = readln().toIntOrNull()
                        if (userAnswerInput == 0) continue

                        if (userAnswerInput == correctAnswerIndex + 1) {
                            println("Правильно!")
                            correctWord.incrementCorrectCount()
                            saveDictionary(dictionary)
                        } else {
                            println("Неправильно! ${correctWord.original} - это ${correctWord.translate}")
                        }
                    }
                }
                2 -> {
                    println("Выбран пункт меню \"Статистика\"")
                    getStatistic(dictionary)
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

fun getStatistic(dictionary: List<Word>) {
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

fun getUserInput(prompt: String = "Ваш ответ:"): Int? {
    println(prompt)
    return readln().toIntOrNull()
}

fun saveDictionary(dictionary: List<Word>) {
    val wordsFile = File("words.txt")
    wordsFile.printWriter().use { out ->
        for (word in dictionary) {
            out.println("${word.original}|${word.translate}|${word.correctAnswersCount}")
        }
    }
    println("Словарь успешно сохранён.")
}

const val COUNT_OF_WORDS = 4