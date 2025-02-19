package ru.maxx52

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0,
) {
    override fun toString(): String {
        return "$original, $translate (правильные ответы: $correctAnswersCount)"
    }
}