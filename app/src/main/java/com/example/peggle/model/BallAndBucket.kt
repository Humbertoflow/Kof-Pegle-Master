package com.example.peggle.model

data class Ball(
    val id: Int = 0,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float = 11f,
    var active: Boolean = true,
    var spin: Float = 0f,
    var spookyRespawnsLeft: Int = 0,
    var fireballActive: Boolean = false,
    var trail: MutableList<Pair<Float, Float>> = mutableListOf(),
    var isMultiballClone: Boolean = false
)

data class Bucket(
    var x: Float,
    var y: Float,
    var width: Float = 100f,
    var height: Float = 26f,
    var vx: Float = 130f,
    var hasPyramidWings: Boolean = false,
    var catchAnimTimer: Float = 0f
) {
    val effectiveWidth: Float
        get() = if (hasPyramidWings) width * 1.55f else width
}

enum class PeggleMaster(
    val id: String,
    val title: String,
    val subtitle: String,
    val powerName: String,
    val powerDesc: String,
    val accentColor: Long,
    val emoji: String
) {
    BJORN(
        id = "bjorn",
        title = "Bjorn",
        subtitle = "El Unicornio Sabio",
        powerName = "Super Guía",
        powerDesc = "Muestra la trayectoria extendida proyectando 3 rebotes precisos.",
        accentColor = 0xFF4A90E2,
        emoji = "🦄"
    ),
    RENFIELD(
        id = "renfield",
        title = "Renfield",
        subtitle = "La Calabaza Espeluznante",
        powerName = "Bola Fantasma",
        powerDesc = "Cuando la bola cae por el fondo, reaparece en la parte superior para una segunda pasada.",
        accentColor = 0xFFFF8A00,
        emoji = "🎃"
    ),
    KAT_TUT(
        id = "kat_tut",
        title = "Kat Tut",
        subtitle = "El Faraón Felino",
        powerName = "Alas Piramidales",
        powerDesc = "Añade alas doradas al cubo inferior, aumentando su tamaño en un 55%.",
        accentColor = 0xFFFFD700,
        emoji = "🐱"
    ),
    MASTER_SPLODER(
        id = "sploder",
        title = "Master Sploder",
        subtitle = "El Señor del Fuego",
        powerName = "Bola Explosiva",
        powerDesc = "Detona una explosión masiva alrededor del pivote verde, destruyendo fichas vecinas.",
        accentColor = 0xFFFF3B30,
        emoji = "💥"
    ),
    WARREN(
        id = "warren",
        title = "Warren",
        subtitle = "El Mago Truquero",
        powerName = "Ruleta Mágica",
        powerDesc = "Gira una ruleta que otorga Triple Puntuación, Bola Extra inmediata o Bola Gigante.",
        accentColor = 0xFF9B51E0,
        emoji = "🎩"
    )
}
