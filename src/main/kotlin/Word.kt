package ru.maxx52

data class Word(
    val questionWord: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
) {
    fun incrementCorrectCount() {
        correctAnswersCount++
    }
}