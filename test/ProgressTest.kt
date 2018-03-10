import org.junit.Assert.*

import org.junit.Test as test

const val PERCENTAGE_DELTA_THRESHOLD = 0.001  // used in assertEquals() calls

const val TEST_LIMIT = 999
const val TEST_CURRENT = 10
const val TEST_DIGITS = 2
const val TEST_PERCENT_DELTA = 0.01

const val TEST_PERCENTAGE = (TEST_CURRENT.toDouble() / TEST_LIMIT) * 100


class ProgressTest {
    /*** Percentage **********************************************************/

    @test fun `percentage is correct`() {
        assertEquals(TEST_PERCENTAGE, mkProgress().percentage, PERCENTAGE_DELTA_THRESHOLD)
    }

    @test fun `percentStr matches percentage`() {
        val expectedStr = String.format("%.2f%%", TEST_PERCENTAGE)
        assertEquals(expectedStr, mkProgress().percentStr)
    }


    /*** Current *************************************************************/

    @test fun `current can accept negative values`() {
        val p = Progress()
        p.current(-1)
        assertEquals(-1, p.current())
    }

    @test fun `current can exceed limits`() {
        val p = Progress()
                .also { it.current(it.limit() - 1) }

        p.update()
        assertEquals(p.limit(), p.current())

        p.update()
        assertEquals(p.limit() + 1, p.current())
    }


    /*** Updates *************************************************************/

    @test fun `current value changes by delta when updated`() {
        val delta = 6
        val p = mkProgress()
        assertEquals(TEST_CURRENT, p.current())
        p.update(delta)
        assertEquals(TEST_CURRENT + delta, p.current())
    }

    @test fun `percentage changes when updated`() {
        val delta = 6
        val expectedPercentage = ((TEST_CURRENT + delta).toDouble() / TEST_LIMIT) * 100
        val p = mkProgress()
        assertEquals(TEST_PERCENTAGE, p.percentage, PERCENTAGE_DELTA_THRESHOLD)
        p.update(delta)
        assertEquals(expectedPercentage, p.percentage, PERCENTAGE_DELTA_THRESHOLD)
    }

    @test fun `update bumps by 1 by default`() {
        val p = Progress()
        val current = p.current()
        p.update()
        assertEquals(current + 1, p.current())
    }

    @test fun `negative updates push current value back`() {
        val p = Progress()
                .also { it.current(10) }

        p.update(-1)
        assertEquals(9, p.current())

        p.update(-9)
        assertEquals(0, p.current())
    }

    @test fun `negative update goes below zero`() {
        val p = Progress()
                .also { it.current(1) }

        p.update(-1)
        assertEquals(0, p.current())

        p.update(-1)
        assertEquals(-1, p.current())
    }


    /*** Helper Functions ****************************************************/

    private fun mkProgress(): Progress = Progress(TEST_LIMIT).apply {
        current(TEST_CURRENT)
        digits(TEST_DIGITS)
        byPercentDelta(TEST_PERCENT_DELTA)
    }
}
