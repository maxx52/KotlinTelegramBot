package ru.androidsprint.englishtrainer.trainer.model

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)