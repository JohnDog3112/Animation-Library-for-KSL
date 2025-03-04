package com.example.mygame

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.random.Random

class Block(
    var position: Vector2,
    var speed: Vector2,
    val color: Color
) {
    val size = 50f
    val rectangle: Rectangle
        get() = Rectangle(position.x, position.y, size, size)
}

class MyGdxGame : ApplicationAdapter() {
    private lateinit var batch: SpriteBatch
    private lateinit var blockTexture: TextureRegion
    private val blocks = mutableListOf<Block>()
    private val screenWidth = 800f
    private val screenHeight = 600f

    override fun create() {
        batch = SpriteBatch()
        blockTexture = TextureRegion(Texture("block.png"))

        // Create multiple blocks with random positions, speeds, and colors
        repeat(5) {
            val position = Vector2(Random.nextFloat() * screenWidth, Random.nextFloat() * screenHeight)
            val speed = Vector2(Random.nextFloat() * 400 - 200, Random.nextFloat() * 400 - 200) // Speed between -200 and 200
            val color = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f)
            blocks.add(Block(position, speed, color))
        }
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val deltaTime = Gdx.graphics.deltaTime

        // Update block positions and handle bouncing off walls
        for (block in blocks) {
            block.position.x += block.speed.x * deltaTime
            block.position.y += block.speed.y * deltaTime

            // Bounce off the walls
            if (block.position.x < 0 || block.position.x + block.size > screenWidth) {
                block.speed.x = -block.speed.x
            }
            if (block.position.y < 0 || block.position.y + block.size > screenHeight) {
                block.speed.y = -block.speed.y
            }
        }

        // Check for collisions between blocks
        for (i in 0 until blocks.size) {
            for (j in i + 1 until blocks.size) {
                val blockA = blocks[i]
                val blockB = blocks[j]
                if (blockA.rectangle.overlaps(blockB.rectangle)) {
                    // Swap speeds on collision for a simple bounce effect
                    val tempSpeed = blockA.speed.cpy()
                    blockA.speed = blockB.speed
                    blockB.speed = tempSpeed
                }
            }
        }

        // Draw all blocks
        batch.begin()
        for (block in blocks) {
            batch.color = block.color // Set the block color
            batch.draw(blockTexture, block.position.x, block.position.y, block.size, block.size)
        }
        batch.end()
        batch.color = Color.WHITE // Reset color to avoid affecting other renders
    }

    override fun dispose() {
        batch.dispose()
        blockTexture.texture.dispose()
    }
}
