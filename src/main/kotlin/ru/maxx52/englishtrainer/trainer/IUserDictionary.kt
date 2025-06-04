package ru.maxx52.englishtrainer.trainer

import ru.maxx52.englishtrainer.trainer.model.Word

interface IUserDictionary {
    fun getNumOfLearnedWords(userId: Long): Int
    fun getSize(): Int
    fun getLearnedWords(userId: Long): List<Word>
    fun getUnlearnedWords(userId: Long): List<Word>
    fun setCorrectAnswersCount(userId: Long, word: String, correctAnswersCount: Int)
    fun restartLearning(userId: Long)
}