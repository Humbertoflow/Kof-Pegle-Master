package com.example.peggle.model

data class ScoreFloater(
    val id: Long,
    val text: String,
    var x: Float,
    var y: Float,
    val color: Long,
    var alpha: Float = 1f,
    val isStyleShot: Boolean = false
)

enum class ParticleType {
    SPARK,
    GLOW_EMBER,
    SHOCKWAVE_RING,
    STAR_SPARKLE
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Long,
    var size: Float,
    var life: Float = 1f,
    val maxLife: Float = 1f,
    val type: ParticleType = ParticleType.SPARK,
    var rotation: Float = 0f,
    var vRot: Float = 0f,
    val growthRate: Float = 0f
)

data class ScoreBin(
    val index: Int,
    val label: String,
    val points: Int,
    val minXRatio: Float,
    val maxXRatio: Float,
    val color: Long
)

enum class ShotPhase {
    AIMING,       // Player aiming cannon
    BALL_IN_PLAY, // Ball bouncing around
    RESOLVING,    // Lit pegs popping, calculating points
    FEVER_SLOWMO, // Slow-motion dramatic freeze on final orange peg
    EXTREME_FEVER // Ode to Joy celebration and score bins drop
}

enum class GameMode {
    ADVENTURE,
    QUICK_PLAY,
    CHALLENGE,
    DUEL
}

data class ShotStatistics(
    var pegsHitInShot: Int = 0,
    var orangeHitInShot: Int = 0,
    var greenHitInShot: Int = 0,
    var purpleHitInShot: Int = 0,
    var multiballHitInShot: Int = 0,
    var longAimHitInShot: Int = 0,
    var blastRadiusHitInShot: Int = 0,
    var shotScore: Int = 0,
    var freeBallAwardedByPoints: Boolean = false,
    var caughtInBucket: Boolean = false,
    var styleShots: MutableList<String> = mutableListOf()
)
