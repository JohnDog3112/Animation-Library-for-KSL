package com.example.mygame.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.example.mygame.ElevatorSimulation

object Lwjgl3Launcher {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration()
        config.setTitle("Elevator Simulation")
        config.setWindowedMode(800, 600) // Set your window size here
        config.setResizable(false)

        Lwjgl3Application(ElevatorSimulation(), config)
    }
}
