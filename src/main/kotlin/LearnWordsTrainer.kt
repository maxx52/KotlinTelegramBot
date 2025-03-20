import java.io.File

data class Statistics(
    val totalCount: Int,
    val learnedCount: List<Word>,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

data class Word(
    val questionWord: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
) {
    fun incrementCorrectCount() {
        correctAnswersCount++
    }
}

class LearnWordsTrainer(
    private val learnedAnswerCount: Int = 3,
    private val countOfQuestionWords: Int = 4,
) {
    private val dictionary = loadDictionary()
    private var question: Question? = null

    fun saveDictionary(words: List<Word>) {
        val wordsFile = File("words.txt")
        wordsFile.printWriter().use { out ->
            for (word in words) {
                out.println("${word.questionWord}|${word.translate}|${word.correctAnswersCount}")
            }
        }
        println("Словарь успешно сохранён.")
    }

    fun getStatistics() : Statistics {
        val totalCount = dictionary.size
        val learnedCount = dictionary.filter { it.correctAnswersCount >= learnedAnswerCount }
        val percent = learnedCount.size * 100 / totalCount
        return Statistics(totalCount, learnedCount, percent)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < learnedAnswerCount }
        if (notLearnedList.isEmpty()) return null
        val variants = if (notLearnedList.size < countOfQuestionWords) {
            val learnedList = dictionary.filter { it.correctAnswersCount >= learnedAnswerCount }.shuffled()
            notLearnedList.shuffled().take(countOfQuestionWords) +
                    learnedList.take(countOfQuestionWords - notLearnedList.size)
        } else {
            notLearnedList.shuffled().take(countOfQuestionWords)
        }.shuffled()
        question = Question(
            variants,
            correctAnswer = notLearnedList.random()
        )
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)

            if (correctAnswerId == userAnswerIndex) {
                it.correctAnswer.incrementCorrectCount()
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): List<Word> {
        try {
            val dictionary = mutableListOf<Word>()
            val wordsFile = File("words.txt")

            if (!wordsFile.exists()) {
                println("Файл не найден: ${wordsFile.absolutePath}")
                return dictionary
            }

            var correctAnswersCount: Int
            val lines: List<String> = wordsFile.readLines()
            for (line in lines) {
                val parts = line.split("|")
                if (parts.size >= 2) {
                    val original = parts[0].trim()
                    val translate = parts[1].trim()
                    correctAnswersCount = parts.getOrNull(2)?.trim()?.toIntOrNull() ?: 0
                    val word = Word(original, translate, correctAnswersCount)
                    dictionary.add(word)
                } else {
                    println("Пропуск строки: '$line' (неправильный формат)")
                }
            }
            return dictionary
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("некорректный файл")
        }
    }
}