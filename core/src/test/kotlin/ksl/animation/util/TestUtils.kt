package ksl.animation.util

import ksl.animation.sim.KSLAnimationLog
import ksl.animation.viewer.AnimationViewer

fun setupAnimationLogTest(viewer: AnimationViewer, setupFileName: String) {
    val testSetup =
        object {}.javaClass.getResourceAsStream(setupFileName)?.bufferedReader()?.readText()

    if (testSetup != null) {
        viewer.loadAnimationSetup(parseJsonToAnimation(testSetup))
        viewer.loadAnimationLog(KSLAnimationLog("", viewer))
    } else {
        throw RuntimeException("Failed to load animation")
    }
}
