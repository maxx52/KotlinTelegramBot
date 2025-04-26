package ru.maxx52.englishtrainer.trainer.model

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
) {
    fun incrementCorrectCount() {
        correctAnswersCount++
    }
}