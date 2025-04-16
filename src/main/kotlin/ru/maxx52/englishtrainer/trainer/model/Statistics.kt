package ru.maxx52.englishtrainer.trainer.model

data class Statistics(
    val totalCount: Int,
    val learnedCount: List<Word>,
    val percent: Int,
)