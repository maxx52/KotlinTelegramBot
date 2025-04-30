package ru.maxx52.englishtrainer.trainer

import ru.maxx52.englishtrainer.trainer.model.Question
import ru.maxx52.englishtrainer.trainer.model.Statistics
import ru.maxx52.englishtrainer.trainer.model.Word

class LearnWordsTrainer(
    private val userId: Long,
    private val dictionary: IUserDictionary,
    private val countOfQuestionWords: Int = 4,
) {
    var currentQuestion: Question? = null
        private set

    fun getStatistics() : Statistics {
        val totalCount = dictionary.getSize()
        val learnedCount = dictionary.getNumOfLearnedWords(userId)
        val percent = learnedCount * 100 / totalCount
        return Statistics(totalCount, learnedCount, percent)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.getUnlearnedWords(userId)
        if (notLearnedList.isEmpty()) return null

        var variants = notLearnedList.shuffled().take(countOfQuestionWords)
        val correctAnswer = variants.random()

        if (variants.size < countOfQuestionWords) {
            val learnedList = dictionary.getLearnedWords(userId).shuffled()
            variants = (variants + learnedList.take(countOfQuestionWords - variants.size))
                .shuffled()
        }
        currentQuestion = Question(
            variants,
            correctAnswer = correctAnswer
        )
        return currentQuestion
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return currentQuestion?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)

            if (correctAnswerId == userAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                dictionary.setCorrectAnswersCount(
                    userId,
                    it.correctAnswer.questionWord,
                    it.correctAnswer.correctAnswersCount
                )
                true
            } else {
                false
            }
        } ?: false
    }

    fun getCurrentQuestion(): Word? {
        return currentQuestion?.correctAnswer
    }

    fun restartLearning() {
        return dictionary.restartLearning(userId)
    }
}