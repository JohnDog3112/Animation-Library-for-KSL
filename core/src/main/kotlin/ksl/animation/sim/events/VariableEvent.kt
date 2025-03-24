package ksl.animation.sim.events

import ksl.animation.sim.KSLLogEvent
import ksl.animation.util.KSLAnimationGlobals
import ksl.animation.viewer.AnimationViewer

class VariableEvent(time: Double, viewer: AnimationViewer) : KSLLogEvent(time, viewer) {
    companion object {
        const val KEYWORD_VARIABLE = "VARIABLE"
        const val KEYWORD_SET = "SET"
    }

    private lateinit var variableID: String
    private lateinit var stringVal: String
    private var doubleVal: Double? = null

    // RESOURCE "RESOURCE ID" SET STATE "NEW STATE"
    override fun parse(tokens: List<String>): Boolean {
        this.startParsing(tokens)

        if (this.parseKeyword() != KEYWORD_VARIABLE) return false

        this.variableID = this.parseVariableId()

        if (this.parseKeyword() != KEYWORD_SET) return false

        this.stringVal = this.parseString()
        this.doubleVal = this.stringVal.toDoubleOrNull()
        return true
    }

    override fun execute() {
        if (KSLAnimationGlobals.VERBOSE) println("Variable Event: $stringVal on $variableID")

        val variable = viewer.variables[variableID] ?: throw RuntimeException("Variable $variableID not found")
        val cpDoubleVal = this.doubleVal
        if (cpDoubleVal != null) {
            variable.setValue(cpDoubleVal)
        } else {
            variable.setValue(this.stringVal)
        }
    }

    override fun involvesObject(objectId: String): Boolean {
        return false
    }
}
