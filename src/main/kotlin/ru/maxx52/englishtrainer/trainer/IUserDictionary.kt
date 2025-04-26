package ru.maxx52.englishtrainer.trainer

import ru.maxx52.englishtrainer.trainer.model.Word

interface IUserDictionary {
    fun getNumOfLearnedWords(userId: Int): Int
    fun getSize(): Int
    fun getLearnedWords(userId: Int): List<Word>
    fun getUnlearnedWords(userId: Int): List<Word>
    fun setCorrectAnswersCount(word: String, correctAnswersCount: Int)
    fun restartLearning(userId: Int)
}