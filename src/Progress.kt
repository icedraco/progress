import kotlin.*
import kotlin.math.round

class Progress(private var _limit: Int = 100, private val start: Int = 0) {
    private var _digitsAfterDecimalPoint = 2
    private var _current = start
    private var currentNotifier: DelayedNotifier = ImmediateNotifier
    private var currentOutput: Output = RegularStringOutput()

    val progressRatio get() = (current().toDouble() / _limit)
    val percentage get() = progressRatio * 100
    val percentStr get() = String.format("%.${_digitsAfterDecimalPoint}f%%", percentage)


    fun reset() {
        current(start)
        currentNotifier.onReset()
    }

    fun current(): Int = _current
    fun current(value: Int) {
        _current = value
    }

    fun limit(): Int = _limit
    fun limit(value: Int) {
        _limit = value
    }

    fun digits(): Int = _digitsAfterDecimalPoint
    fun digits(nDigits: Int) {
        if (nDigits < 0)
            throw IllegalArgumentException("Number of digits must be non-negative ($nDigits)")

        _digitsAfterDecimalPoint = nDigits
    }

    fun update(delta: Int = 1, notifyCallback: (Progress) -> Unit = {}) {
        current(current() + delta)
        currentNotifier.onProgressUpdate(this, notifyCallback)
    }

    fun alwaysNotify() = this.apply {
        currentNotifier = ImmediateNotifier
    }

    fun byCurrentDelta(currentDelta: Int) = this.apply {
        currentNotifier = CurrentDeltaNotifier(currentDelta)
    }

    fun byPercentDelta(delta: Double) = this.apply {
        currentNotifier = PercentageDeltaNotifier(delta)
    }

    fun byTimeDelta(millis: Long) = this.apply {
        currentNotifier = TimeDeltaNotifier(millis)
    }

    fun byOutputsPerSecond(ops: Double) = this.apply {
        byTimeDelta((1000.0 / ops).toLong())
    }

    fun useOutput(output: Output) = this.apply {
        currentOutput = output
    }

    fun useRegularOutput(what: String = "") =
            useOutput(RegularStringOutput(what))

    fun useProgressBar(width: Int = 25, progressChar: Char = '=', prefix: String = "[", suffix: String = "]") =
            useOutput(ProgressBarStringOutput(width, progressChar, prefix, suffix))


    /*** String Output *******************************************************/

    override fun toString(): String = currentOutput.string(this)

    interface Output {
        fun string(p: Progress): String
        fun callback(p: Progress) {
            println(string(p))
        }
    }

    class RegularStringOutput(private val what: String = "") : Output {
        override fun string(p: Progress): String = "${p.current()}/${p.limit()} $what (${p.percentStr})"
    }

    class ProgressBarStringOutput(
            val width: Int,
            val progressChar: Char = '=',
            val prefix: String = "[",
            val suffix: String = "]")
        : Output {

        override fun string(p: Progress): String {
            val progressRatio = p.progressRatio
            val numProgressChars = when {
                progressRatio > 1.0 -> width
                progressRatio < 0.0 -> 0
                else -> (progressRatio * width).toInt()
            }

            val sb = StringBuilder()
            sb.append(prefix)
            sb.append(String().padStart(numProgressChars, progressChar).padEnd(width))
            sb.append(suffix)
            sb.append(" ${p.percentStr} (${p.current()}/${p.limit()})")
            return sb.toString()
        }
    }



    /*** Notification ********************************************************/

    interface DelayedNotifier {
        fun onReset()
        fun onProgressUpdate(progress: Progress, callback: (Progress) -> Unit)
    }

    private object ImmediateNotifier : DelayedNotifier {
        override fun onReset() { /* do nothing */ }
        override fun onProgressUpdate(progress: Progress, callback: (Progress) -> Unit) {
            callback(progress)
        }
    }

    private inner class CurrentDeltaNotifier(private val delta: Int) : DelayedNotifier {
        private var last = 0

        override fun onReset() {
            last = 0
        }

        override fun onProgressUpdate(progress: Progress, callback: (Progress) -> Unit) {
            val current = progress.current()
            val currentDelta = current - last
            if (currentDelta >= delta) {
                callback(progress)
                last = current
            }
        }
    }

    private inner class PercentageDeltaNotifier(private val delta: Double) : DelayedNotifier {
        private var nextPercentage = delta

        override fun onReset() {
            nextPercentage = delta
        }

        override fun onProgressUpdate(progress: Progress, callback: (Progress) -> Unit) {
            if (progress.percentage >= nextPercentage) {
                callback(progress)
                nextPercentage = progress.percentage + delta
            }
        }
    }

    private inner class TimeDeltaNotifier(private val deltaMillis: Long) : DelayedNotifier {
        private var last = 0L
        override fun onReset() {
            last = 0L
        }

        override fun onProgressUpdate(progress: Progress, callback: (Progress) -> Unit) {
            val current = System.currentTimeMillis()
            val currentDelta = current - last
            if (currentDelta >= deltaMillis) {
                callback(progress)
                last = current
            }
        }
    }
}