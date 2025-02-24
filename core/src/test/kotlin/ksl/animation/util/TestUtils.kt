package ksl.animation.util

import ksl.animation.sim.KSLAnimationLog
import ksl.animation.viewer.AnimationViewer

fun setupAnimationLogTest(animationViewer: AnimationViewer, setupFileName: String) {
    val testSetup =
        object {}.javaClass.getResourceAsStream(setupFileName)?.bufferedReader()?.readText()

    if (testSetup != null) {
        animationViewer.loadAnimation(parseJsonToAnimation(testSetup), KSLAnimationLog("", animationViewer))
    } else {
        throw RuntimeException("Failed to load animation")
    }
}
