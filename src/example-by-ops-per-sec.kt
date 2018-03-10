fun main(args: Array<String>) {
    val progress = Progress(100)
            .byOutputsPerSecond(3.0)

    for (i in 0 until 100) {
        progress.update { println(it) }
        Thread.sleep(100)
    }
}