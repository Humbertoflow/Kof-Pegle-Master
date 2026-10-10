package com.example.peggle.model

enum class PegType {
    BLUE,         // Standard peg (base points)
    ORANGE,       // Primary goal (must clear all 25 to win!)
    GREEN,        // Magic peg (triggers Peggle Master power)
    PURPLE,       // Score booster (rotates each shot, high points)
    MULTIBALL,    // Special Power-Up: splits ball into 2 extra active balls
    LONG_AIM,     // Special Power-Up: grants extended laser trajectory and bounce reach
    BLAST_RADIUS; // Special Power-Up: shockwave explosion clears neighbouring cluster

    val isSpecialPowerUp: Boolean
        get() = this == MULTIBALL || this == LONG_AIM || this == BLAST_RADIUS
}

enum class PegShape {
    CIRCLE,
    BRICK
}

data class Peg(
    val id: Int,
    var x: Float,
    var y: Float,
    var type: PegType,
    val shape: PegShape = PegShape.CIRCLE,
    var width: Float = 14f,   // Radius for circle, width for brick
    var height: Float = 14f,  // Radius for circle, height for brick
    var rotation: Float = 0f, // Rotation in degrees for bricks
    var isHit: Boolean = false,
    var isRemoved: Boolean = false,
    var hitOrder: Int = -1,
    var hitTimeMs: Long = 0L,
    var popProgress: Float = 0f // 0f to 1f for removal animation
) {
    val radius: Float
        get() = width

    val isSpecialPowerUp: Boolean
        get() = type == PegType.MULTIBALL || type == PegType.LONG_AIM || type == PegType.BLAST_RADIUS
}
