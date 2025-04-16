package ru.maxx52.englishtrainer.trainer.model

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)