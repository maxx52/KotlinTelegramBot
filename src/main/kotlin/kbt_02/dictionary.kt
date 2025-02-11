package ru.maxx52.kbt_02

import java.io.File

fun main() {

    val wordsFile = File("words.txt")
    wordsFile.createNewFile()
    wordsFile.appendText("hello привет\n")
    wordsFile.appendText("dog собака\n")
    wordsFile.appendText("cat кошка\n")

    println(wordsFile.readLines())
    wordsFile.forEachLine { line: String -> println(line) }
}