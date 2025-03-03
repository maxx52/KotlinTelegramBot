package ru.maxx52

import java.lang.NumberFormatException

fun getUserInput(prompt: String = "Ваш ответ:"): Int? {
    println(prompt)
    return readln().toIntOrNull()
}

fun Question.asConsoleString(): String {
    val variants = this.variants
        .mapIndexed { index: Int, word: Word -> " ${index + 1} - ${word.translate}" }
        .joinToString(separator = "\n")
    return this.correctAnswer.questionWord + "\n" + variants + "\n 0 - выйти в меню"
}

const val COUNT_OF_WORDS = 4

fun main() {
    val trainer = LearnWordsTrainer()

    while (true) {
        println("Меню:")
        println("1 - Учить слова")
        println("2 - Статистика")
        println("0 - Выход")

        try {
            val inputMenu = getUserInput()
            when (inputMenu) {
                1 -> {
                    val question = trainer.getNextQuestion()
                    println("Выбран пункт меню \"Учить слова\"")

                    if (question == null) {
                        println("Все слова в словаре выучены")
                        continue
                    } else {
                        println("-------------")
                        println(question.asConsoleString())

                        val userAnswerInput = readln().toIntOrNull()
                        if (userAnswerInput == 0) break

                        if (trainer.checkAnswer(userAnswerInput?.minus(1))) {
                            println("Правильно!\n")
                            trainer.saveDictionary(question.variants)
                        } else {
                            println("Неправильно! ${question.correctAnswer.questionWord} - это ${question.correctAnswer.translate}")
                        }
                    }
                }
                2 -> {
                    println("Выбран пункт меню \"Статистика\"")
                    val statistics = trainer.getStatistic()
                    println("Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent}")
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