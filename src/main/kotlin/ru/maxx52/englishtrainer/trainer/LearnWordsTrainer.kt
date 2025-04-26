package ru.maxx52.englishtrainer.trainer

import ru.maxx52.englishtrainer.trainer.model.Question
import ru.maxx52.englishtrainer.trainer.model.Word

class LearnWordsTrainer {
    var currentQuestion: Question? = null
        private set

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return currentQuestion?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer) + 1

            if (correctAnswerId == userAnswerIndex) {
                it.correctAnswer.incrementCorrectCount()
                FileUserDictionary().saveDictionary()
                true
            } else {
                false
            }
        } ?: false
    }

    fun getCurrentQuestion(): Word? {
        return currentQuestion?.correctAnswer
    }
}