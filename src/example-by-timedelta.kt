fun main(args: Array<String>) {
    val progress = Progress(100)
            .byTimeDelta(1000)

    for (i in 0 until 100) {
        progress.update { println(it) }
        Thread.sleep(100)
    }
}