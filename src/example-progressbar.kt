fun main(args: Array<String>) {
    val progress = Progress(100)
            .useProgressBar()

    for (i in 0 until 100) {
        progress.update { println(it) }
    }
}