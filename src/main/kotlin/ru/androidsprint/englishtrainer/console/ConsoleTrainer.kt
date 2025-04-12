package ru.androidsprint.englishtrainer.console

import ru.androidsprint.englishtrainer.trainer.LearnWordsTrainer
import ru.androidsprint.englishtrainer.trainer.model.Question
import ru.androidsprint.englishtrainer.trainer.model.Word
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

fun main() {
    val trainer = try {
        LearnWordsTrainer()
    } catch (e: Exception) {
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
        } catch (e: NumberFormatException) {
            println("Введите число 1, 2 или 0")
        }
    }
}