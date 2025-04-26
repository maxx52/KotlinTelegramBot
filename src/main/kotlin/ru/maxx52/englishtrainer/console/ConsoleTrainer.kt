package ru.maxx52.englishtrainer.console

import ru.maxx52.englishtrainer.trainer.FileUserDictionary
import ru.maxx52.englishtrainer.trainer.LearnWordsTrainer
import ru.maxx52.englishtrainer.trainer.model.Question
import ru.maxx52.englishtrainer.trainer.model.Word
import java.lang.NumberFormatException

fun getUserInput(prompt: String = "Ваш ответ:"): Int? {
    println(prompt)
    return readln().toIntOrNull()
}

fun Question.asConsoleString(): String {
    val variants = this.variants
        .mapIndexed { index: Int, word: Word -> " ${index + 1} - ${word.translate}" }
        .joinToString(separator = "\n")
    return this.correctAnswer.original + "\n" + variants + "\n 0 - выйти в меню"
}

fun main() {
    val trainer = try {
        FileUserDictionary()
    } catch (_: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    while (true) {
        println("Меню:")
        println("1 - Учить слова")
        println("2 - Статистика")
        println("0 - Выход")

        try {
            when (getUserInput()) {
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
                        if (userAnswerInput == 0) continue

                        if (LearnWordsTrainer().checkAnswer(userAnswerInput?.minus(1))) {
                            println("Правильно!\n")
                            trainer.saveDictionary()
                        } else {
                            println("Неправильно! ${question.correctAnswer.original} - это ${question.correctAnswer.translate}")
                        }
                    }
                }
                2 -> {
                    println("Выбран пункт меню \"Статистика\"")
                    val statistics = trainer.getStatistics()
                    println("Выучено ${statistics.learnedCount.size} из ${statistics.totalCount} слов | ${statistics.percent} %")
                }
                0 -> {
                    println("Выход из программы")
                    return
                }
                else -> {
                    println("Введите число 1, 2 или 0")
                }
            }
        } catch (_: NumberFormatException) {
            println("Введите число 1, 2 или 0")
        }
    }
}