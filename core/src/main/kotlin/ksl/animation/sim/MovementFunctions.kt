package ksl.animation.sim

import java.lang.Math.clamp
import kotlin.math.pow

object MovementFunctions {
    const val INSTANT_FUNCTION = "INSTANT"
    const val LINEAR_FUNCTION = "LINEAR"
    const val EASE_IN_FUNCTION = "EASE_IN"
    const val EASE_OUT_FUNCTION = "EASE_OUT"
    const val EASE_IN_OUT_FUNCTION = "EASE_IN_OUT"

    fun applyFunction(function: String, value: Double): Double {
        return when (function) {
            INSTANT_FUNCTION -> 1.0
            LINEAR_FUNCTION -> value
            EASE_IN_FUNCTION -> easeInCubic(value)
            EASE_OUT_FUNCTION -> easeOutCubic(value)
            EASE_IN_OUT_FUNCTION -> easeInOutCubic(value)
            else -> throw RuntimeException("Unknown function: $function")
        }
    }

    private fun easeInCubic(value: Double): Double {
        return clamp(value.pow(3), 0.0, 1.0)
    }

    private fun easeOutCubic(value: Double): Double {
        return clamp(1 - (1 - value).pow(3), 0.0, 1.0)
    }

    private fun easeInOutCubic(value: Double): Double {
        return if (value < 0.5) clamp(4 * value.pow(3), 0.0, 1.0)
        else clamp(1 - (-2 * value + 2).pow(3) / 2, 0.0, 1.0)
    }
}
