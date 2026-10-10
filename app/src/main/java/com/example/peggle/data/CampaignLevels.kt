package com.example.peggle.data

import com.example.R
import com.example.peggle.model.PegShape
import com.example.peggle.model.PegType
import com.example.peggle.model.PeggleMaster
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Provides 200 additional campaign levels (Stages 9 to 208) for the KOF Peggle Deluxe campaign.
 * Each level features distinct thematic peg layouts, background art, Masters, and obstacles.
 */
object CampaignLevels {

    fun getAdditionalLevels(): List<LevelData> {
        return (9..208).map { stageNum ->
            createLevel(stageNum)
        }
    }

    private fun createLevel(stageNum: Int): LevelData {
        val master = when (stageNum % 5) {
            0 -> PeggleMaster.WARREN
            1 -> PeggleMaster.BJORN
            2 -> PeggleMaster.RENFIELD
            3 -> PeggleMaster.KAT_TUT
            else -> PeggleMaster.MASTER_SPLODER
        }

        val (bgRes, gradient) = when (stageNum % 10) {
            0 -> Pair(R.drawable.bg_kof_orochi, Pair(0xFF28133EL, 0xFF431F6BL))
            1 -> Pair(R.drawable.bg_kof_kyo, Pair(0xFF132B47L, 0xFF19402AL))
            2 -> Pair(R.drawable.bg_kof_southtown, Pair(0xFF1B2A4AL, 0xFF2B4D3FL))
            3 -> Pair(R.drawable.bg_kof_mai, Pair(0xFF2B1238L, 0xFF4A1E14L))
            4 -> Pair(R.drawable.bg_kof_iori, Pair(0xFF1E1128L, 0xFF3D1630L))
            5 -> Pair(R.drawable.bg_anime_forest, Pair(0xFF0F261EL, 0xFF183D2AL))
            6 -> Pair(R.drawable.bg_anime_gothic, Pair(0xFF1A142BL, 0xFF2C193DL))
            7 -> Pair(R.drawable.bg_anime_desert, Pair(0xFF33200BL, 0xFF543D16L))
            8 -> Pair(R.drawable.bg_anime_volcano, Pair(0xFF45140EL, 0xFF692418L))
            else -> Pair(R.drawable.bg_anime_cosmic, Pair(0xFF0C1636L, 0xFF1F225CL))
        }

        val title = getStageTitle(stageNum)
        val subtitle = getStageSubtitle(stageNum, master)
        val templates = generateTemplatesForStage(stageNum)

        return LevelData(
            id = "level_$stageNum",
            stageNumber = stageNum,
            title = title,
            subtitle = subtitle,
            master = master,
            backgroundGradient = gradient,
            bgDrawableRes = bgRes,
            templates = templates
        )
    }

    private fun getStageTitle(stage: Int): String = when (stage) {
        9 -> "Espiral de Fuego Solar"
        10 -> "Doble Eclipse de Orochi"
        11 -> "Muralla de South Town"
        12 -> "Danza de Abanicos Ninja"
        13 -> "Laberinto Carmesí de Iori"
        14 -> "Corona Solar Kusanagi"
        15 -> "Diamante del Faraón"
        16 -> "Caldera Yasakani"
        17 -> "Olas Cósmicas del Torneo"
        18 -> "Reloj de Arena Ancestral"
        19 -> "Trono del Gran Campeón"
        20 -> "Templo del Dragón de Fuego"
        21 -> "Hexágono Místico KOF"
        22 -> "Constelación Peggle"
        23 -> "Cascada Ninja de Loto"
        24 -> "Vórtice Oscuro Orochi"
        25 -> "Escudo Sagrado de Yata"
        26 -> "Rueda de la Fortuna KOF"
        27 -> "Cañón del Fénix"
        28 -> "Tres Tesoros Sagrados"
        29 -> "Tormenta de Neón Callejera"
        30 -> "Cruz de Batalla Extrema"
        31 -> "Anillos Celestiales"
        32 -> "Selva Esmeralda de Furia"
        33 -> "Fortaleza Flotante"
        34 -> "Ojo del Huracán Kusanagi"
        35 -> "Flechas del Juicio"
        36 -> "Ruinas Ancestrales de Shiranui"
        37 -> "Geometría Sagrada de Kat Tut"
        38 -> "Símbolo Infinito de Iori"
        39 -> "Coliseo de los Gladiadores"
        40 -> "Columnas Gemelas de Magma"
        41 -> "Cristales de Poder Cósmico"
        42 -> "Péndulo de Alta Precisión"
        43 -> "Dunas del Desierto Místico"
        44 -> "Laberinto de los Recuerdos"
        45 -> "Baluarte Imperial KOF"
        46 -> "Rapsodia de Rebotes"
        47 -> "Puerta Torii Espiritual"
        48 -> "Cometa Carmesí Yasakani"
        49 -> "Corazón del Luchador"
        50 -> "Furia del Trueno Celestial"
        51 -> "Bastión Helado del Torneo"
        52 -> "Órbita Lunar de Iori"
        53 -> "Prisma de Luz Kusanagi"
        54 -> "Esfinge del Triunfo"
        55 -> "Juicio de los 5 Maestros"
        56 -> "Grieta Dimensional Orochi"
        57 -> "Vórtice del Apocalipsis"
        58 -> "La Gran Final: Rey de Peggle"
        59 -> "Despertar de Orochi"
        60 -> "Tormenta de Viento de Goenitz"
        61 -> "Furia Eléctrica de Shermie"
        62 -> "Llamas del Destino de Chris"
        63 -> "Tierra Temblorosa de Yashiro"
        64 -> "Sello de los Tres Clanes"
        65 -> "Laberinto de la Fortaleza NESTS"
        66 -> "Puño Ardiente de K'"
        67 -> "Vapor Helado de Kula Diamond"
        68 -> "Látigo de Acero de Whip"
        69 -> "Fuerza Bruta de Maxima"
        70 -> "Transformación de K9999"
        71 -> "Garras de Nameless"
        72 -> "Alfileres de Foxy"
        73 -> "Cuchillas de Diana"
        74 -> "Cyborg de Clon Kyo"
        75 -> "Prototipo Kripke"
        76 -> "Rayo Láser de Zero"
        77 -> "Capa Gravitacional de Clon Zero"
        78 -> "Trono Espacial de Igniz"
        79 -> "Caída del Satélite NESTS"
        80 -> "Llamas Esmeralda de Ash Crimson"
        81 -> "Rosa Púrpura de Elisabeth"
        82 -> "Capricho de Shen Woo"
        83 -> "Asesinato Silencioso de Duo Lon"
        84 -> "Viento Afilado de Oswald"
        85 -> "Rayo del Dragón de Sie Kensou"
        86 -> "Esfera Psíquica de Athena"
        87 -> "Bebida del Sabio Chin Gentsai"
        88 -> "Garra de Halcón de Kula"
        89 -> "Ataque Furioso de Terry Bogard"
        90 -> "Patada Huracán de Joe Higashi"
        91 -> "Gancho Letal de Andy Bogard"
        92 -> "Paso de Sombras de Mai Shiranui"
        93 -> "Impacto Galáctico de Ralf Jones"
        94 -> "Bomba Vulcan de Clark Still"
        95 -> "Rebel Spark de Leona Heidern"
        96 -> "Corte Láser del Comandante Heidern"
        97 -> "Filo Dorado de Adelheid"
        98 -> "Legado de Rugal Bernstein"
        99 -> "Genocide Cutter Prohibido"
        100 -> "Dios de la Destrucción Omega Rugal"
        101 -> "Guadaña de la Muerte de Mukai"
        102 -> "Piedra Ancestral de Shion"
        103 -> "Cadenas Temporales de Magaki"
        104 -> "Portal del Tiempo de Saiki"
        105 -> "Desvanecimiento del Vacío Temporal"
        106 -> "Guanteletes de Shun'ei"
        107 -> "Almohada del Sueño de Meitenkun"
        108 -> "Lanza de Sombras de Kukri"
        109 -> "Vórtice Misterioso de Verse"
        110 -> "Rencor Cósmico de Otoma=Raga"
        111 -> "Danza del Abanico de Fuego"
        112 -> "Loto Sagrado de Chizuru Kagura"
        113 -> "Campana del Juicio de Yata"
        114 -> "Doble Espejo de Ilusiones"
        115 -> "Ráfaga Solar Kusanagi 108 Shiki"
        116 -> "Orochinagi: Gran Dragón"
        117 -> "Mu Shiki: La Nada Definitiva"
        118 -> "Ya Otome: Ocho Doncellas"
        119 -> "Saika: Flor de la Agonía Carmesí"
        120 -> "Fuego Fatuo de Yasakani"
        121 -> "Garra de Acero de Choi Bounge"
        122 -> "Bola de Hierro de Chang Koehan"
        123 -> "Justicia Taekwondo de Kim Kaphwan"
        124 -> "Patada Fénix Ascendente"
        125 -> "Vuelo de la Grulla de Luong"
        126 -> "Poder del Kárate Kyokugen"
        127 -> "Haoh Shoukou Ken de Ryo Sakazaki"
        128 -> "Golpe del Dragón de Robert"
        129 -> "Espíritu Inquebrantable de Takuma"
        130 -> "Encanto de Yuri Sakazaki"
        131 -> "Ilusión de Golpes de King"
        132 -> "Tornado Kick de Kasumi Todoh"
        133 -> "Palma Rompedora de Todoh"
        134 -> "Locura Asesina de Ryuji Yamazaki"
        135 -> "Mordida de Serpiente"
        136 -> "Giro Mortal de Blue Mary"
        137 -> "Disparo Eléctrico de Billy Kane"
        138 -> "Bastón de Tres Secciones"
        139 -> "Puño Pesado de Antonov"
        140 -> "Campeonato Mundial KOF"
        141 -> "Arenas Movedizas de Najd"
        142 -> "Manto de las Tinieblas"
        143 -> "Garras Felinas de Isla"
        144 -> "Grafiti Rebelde de Amanda"
        145 -> "Golpe Espiritual de Dolores"
        146 -> "Tierra Ancestral Africana"
        147 -> "Duelo de Gigantes en el Ring"
        148 -> "Impacto Meteórico de Ramon"
        149 -> "Máscara Dorada de Dinosaurs"
        150 -> "Mordisco Jurásico Extremo"
        151 -> "Academia Peggle: Examen Maestro"
        152 -> "Guía Celestial de Bjorn"
        153 -> "Tiro Milagroso del Unicornio"
        154 -> "Super Guía de 4 Rebotes"
        155 -> "Mansión Encantada de Renfield"
        156 -> "Danza Espeluznante de Calabazas"
        157 -> "Bola Fantasma: Resurrección Eterna"
        158 -> "Pirámide Dorada de Kat Tut"
        159 -> "Alas del Faraón Desplegadas"
        160 -> "Momia Guardiana del Templo"
        161 -> "Magma Hirviente de Master Sploder"
        162 -> "Erupción en Cadena Volcánica"
        163 -> "Detonación de 50 Clavijas"
        164 -> "Gran Ruleta de Warren"
        165 -> "Conejo Mágico de la Fortuna"
        166 -> "Puntos Triples y Bolas Extras"
        167 -> "Castillo Celestial de Neón"
        168 -> "Lluvia de Gemas Púrpuras"
        169 -> "Fiebre Extrema: Gloria Total"
        170 -> "Sinfonía de Beethoven en el Espacio"
        171 -> "Prueba de Precisión Cuántica"
        172 -> "Rebotes Parabólicos de Newton"
        173 -> "Gravedad Cero en la Estratosfera"
        174 -> "Campo de Fuerza Electromagnético"
        175 -> "Prisma de Espejos Angulares"
        176 -> "Laberinto Hexagonal de Neón"
        177 -> "Túnel de Aceleración Sónica"
        178 -> "Rebote Perfecto en el Vértice"
        179 -> "Cataratas de Oro Líquido"
        180 -> "Anillos Entrelazados de Saturno"
        181 -> "Espiral Áurea de Fibonacci"
        182 -> "Geometría Sagrada de Peggle"
        183 -> "Tríada de Fuego, Luz y Sombra"
        184 -> "Doble Bumper de Alta Velocidad"
        185 -> "Embudo de la Muerte Súbita"
        186 -> "Lluvia de Meteoros Naranja"
        187 -> "La Última Clavija Verde"
        188 -> "Canasta Veloz de 100.000 Puntos"
        189 -> "Tiro Milimétrico al Horizonte"
        190 -> "Reacción Termonuclear en Cadena"
        191 -> "El Reto del Gran Maestro KOF"
        192 -> "Colisión de Dos Mundos"
        193 -> "Danza de Fuego y Magia Arcana"
        194 -> "El Santuario del Rey de los Pegs"
        195 -> "Alas de Fénix Dorado"
        196 -> "Furia del Dragón Esmeralda"
        197 -> "Eclipse Cósmico Definitivo"
        198 -> "Resonancia Armónica Universal"
        199 -> "Oda a la Alegría: Clímax Sinfónico"
        200 -> "El Bautismo del Gran Campeón"
        201 -> "Ascenso al Monte Olimpo KOF"
        202 -> "Puerta de las Cien Victorias"
        203 -> "El Ojo de Orochi Despierto"
        204 -> "Llama Carmesí Eterna de Kyo e Iori"
        205 -> "Poder Máximo: 200 Combo Strike"
        206 -> "El Juicio Final de los Cinco Maestros"
        207 -> "La Batalla del Siglo: Peggle x KOF"
        208 -> "¡El Rey Supremo Inmortal de Peggle!"
        else -> "Misión Épica $stage"
    }

    private fun getStageSubtitle(stage: Int, master: PeggleMaster): String = when (stage) {
        9 -> "Kyo Kusanagi despliega espirales flamígeras de precisión"
        10 -> "Warren y el poder del eclipse místico en South Town"
        11 -> "Defiende las murallas con rebotes calculados de Bjorn"
        12 -> "Mai Shiranui lanza sus abanicos en abanico giratorio"
        13 -> "Renfield desata los zombis en el laberinto de fuego púrpura"
        14 -> "Kat Tut invoca la corona protectora del sol naciente"
        15 -> "Alinea los ángulos dorados en la cámara sagrada"
        16 -> "Master Sploder detona la caldera con explosiones en cadena"
        17 -> "Surfea las ondas de energía entre las dimensiones"
        18 -> "El tiempo corre: calcula cada tiro antes de que caiga la arena"
        19 -> "Reta a los mejores combatientes en el trono supremo"
        20 -> "Desciende por la espalda del dragón con tiros de fantasía"
        21 -> "Estructura hexagonal con rebotes simétricos perfectos"
        22 -> "Conecta los puntos cósmicos en el cielo nocturno"
        23 -> "Esquiva las hojas acuáticas en el jardín sagrado"
        24 -> "La fuerza de atracción del vórtice prueba tu temple"
        25 -> "Escudo impenetrable con ladrillos de absorción de impacto"
        26 -> "Warren hace girar la gran ruleta de puntos dobles"
        27 -> "Desfiladero estrecho donde el balón toma velocidad terminal"
        28 -> "El espejo, la espada y la joya en armonía de física"
        29 -> "Ambiente cyberpunk con pinballs y luces estroboscópicas"
        30 -> "Cuatro cuadrantes de acción simultánea y alto riesgo"
        31 -> "Orbita entre los halos concéntricos de alta gravedad"
        32 -> "Ramas densas y lianas que desvían cada disparo"
        33 -> "Plataformas móviles en suspensión sobre el abismo"
        34 -> "El centro de la tormenta es el único camino seguro"
        35 -> "Formación en punta de flecha que premia los tiros certeros"
        36 -> "Columnas milenarias listas para colapsar en Fever Mode"
        37 -> "Triángulos equiláteros concéntricos de máxima dificultad"
        38 -> "Circuito cerrado sin fin: domina el rebote perpetuo"
        39 -> "El rugido de la multitud celebra cada Orange Peg eliminado"
        40 -> "Dos torrentes de fuego que dividen el tablero en dos zonas"
        41 -> "Fragmentos prismáticos que multiplican el puntaje"
        42 -> "Oscilación rítmica que exige sincronización total"
        43 -> "Arena movediza que ralentiza los rebotes laterales"
        44 -> "Encuentra la salida entre los muros fortificados"
        45 -> "Muralla octagonal con gemas mágicas en el centro"
        46 -> "Sinfonía armónica donde cada clavija suena como victoria"
        47 -> "Paso sagrado entre el mundo terrenal y el torneo de Orochi"
        48 -> "Velocidad supersónica en la cola del cometa"
        49 -> "La esencia del espíritu indomable de los luchadores KOF"
        50 -> "Rayos y centellas electrifican la canasta y el lanzador"
        51 -> "Superficies ultra resbaladizas con rebotes elásticos"
        52 -> "Gravedad alterada bajo la influencia de la luna llena"
        53 -> "Descompón la luz blanca en miles de chispas de estilo"
        54 -> "El enigma milenario resuelto con el tiro final"
        55 -> "Cada zona del tablero rinde homenaje a un Gran Maestro"
        56 -> "Dos mitades del tablero conectadas por portales de rebote"
        57 -> "65 clavijas en una vorágine de caos y gloria extrema"
        58 -> "¡El enfrentamiento definitivo por la corona del Rey de Peggle!"
        59 -> "La oscuridad de Orochi envuelve las clavijas en espirales dobles"
        60 -> "Ráfagas cortantes desvían tu bola en trayectorias impredecibles"
        61 -> "Corrientes eléctricas de alto voltaje entre bloques de rebote"
        62 -> "Las llamas negras de Chris consumen cada clavija naranja"
        63 -> "Fuerza telúrica que sacude los pilares y multiplica combos"
        64 -> "Sella los tres orbes sagrados para liberar el poder supremo"
        65 -> "Estructura cibernética con deflectores de energía y pasillos"
        66 -> "Calor abrasador que detona explosiones triples al impactar"
        67 -> "El aliento gélido congela clavijas y crea rebotes de diamante"
        68 -> "Látigos metálicos forman barreras diagonales en el centro"
        69 -> "Blindaje de acero reforzado que exige disparos de potencia"
        70 -> "Brazo mutante gigante que abarca medio tablero con obstáculos"
        71 -> "Cuchillas de titanio guían la bola hacia la canasta dorada"
        72 -> "Agujas de acupuntura que alinean tiros milimétricos"
        73 -> "Doble filo cortante que divide el flujo en dos cascadas"
        74 -> "Clones cibernéticos alineados en filas de alta densidad"
        75 -> "Cámara de pruebas cuántica con rebotes de aceleración"
        76 -> "Cañón de iones que cruza el campo de arriba hacia abajo"
        77 -> "Gravedad cero en el núcleo: la bola flota entre clavijas"
        78 -> "Trono divino en la estratosfera: sólo tiros perfectos ganan"
        79 -> "Lluvia de escombros espaciales con multiplicadores dorados"
        80 -> "Fuego verde esmeralda que danza al compás de tus aciertos"
        81 -> "Elegancia aristocrática y precisión quirúrgica en cada tiro"
        82 -> "Puños de hierro que rompen clavijas y rebotan con violencia"
        83 -> "Espíritus asesinos acechan en las esquinas del tablero"
        84 -> "Cartas de póker afiladas como navajas forman las rampas"
        85 -> "El poder del dragón despierta con cada clavija verde alcanzada"
        86 -> "Esferas de energía psíquica que flotan y absorben impactos"
        87 -> "El estilo del borracho produce rebotes erráticos y geniales"
        88 -> "Garras de hielo cristalino que reflejan destellos luminosos"
        89 -> "Power Wave barre la hilera inferior garantizando la canasta"
        90 -> "Torbellinos ascendentes que devuelven la bola a la cima"
        91 -> "Velocidad ninja y clones de sombra en el pasillo central"
        92 -> "Abanicos dobles que se abren en abanico al primer impacto"
        93 -> "El golpe más pesado del torneo KOF estremece el cuadrilátero"
        94 -> "Llaves de lucha libre que atrapan y propulsan la bola con fuerza"
        95 -> "El despertar del Riot of the Blood enciende el Extreme Fever"
        96 -> "Estrategia militar de precisión con ángulos de 45 grados"
        97 -> "El joven heredero demuestra su maestría en la corte dorada"
        98 -> "El barco portaaviones Black Noah como telón de fondo bélico"
        99 -> "La patada cortante más temida del mundo KOF desafía tu puntería"
        100 -> "¡El jefe supremo Omega Rugal reta tu habilidad total en Peggle!"
        else -> "Domina las clavijas de la etapa $stage y alcanza el Extreme Fever con ${master.title}"
    }

    private fun generateTemplatesForStage(stage: Int): List<PegTemplate> {
        val list = mutableListOf<PegTemplate>()
        val pattern = stage % 20

        when (pattern) {
            0 -> {
                // Dual Eclipse / Concentric Rings
                generateRings(list, count1 = 14, r1 = 0.28f, count2 = 20, r2 = 0.38f, cx = 0.5f, cy = 0.44f)
                generateBottomHopper(list, count = 7, y = 0.76f)
            }
            1 -> {
                // Archimedean Spiral
                generateSpiral(list, turns = 2.5f, points = 36, maxRadius = 0.38f, cx = 0.5f, cy = 0.45f)
                list.add(PegTemplate(0.18f, 0.38f, shape = PegShape.BRICK, width = 42f, height = 13f, rotation = 40f))
                list.add(PegTemplate(0.82f, 0.38f, shape = PegShape.BRICK, width = 42f, height = 13f, rotation = -40f))
                generateBottomHopper(list, count = 8, y = 0.74f)
            }
            2 -> {
                // Fortress Wall / Battlements with Chutes
                generateGridColumns(list, cols = 7, rows = 4, startY = 0.30f, spacingY = 0.07f)
                list.add(PegTemplate(0.32f, 0.62f, shape = PegShape.BRICK, width = 46f, height = 14f, rotation = -20f))
                list.add(PegTemplate(0.68f, 0.62f, shape = PegShape.BRICK, width = 46f, height = 14f, rotation = 20f))
                generateBottomHopper(list, count = 7, y = 0.78f)
            }
            3 -> {
                // Fan Arcs (Danza de Abanicos)
                generateFanArcs(list, arcs = 3, pegsPerArc = 12, startRadius = 0.22f, step = 0.08f)
                generateBottomHopper(list, count = 8, y = 0.75f)
            }
            4 -> {
                // Zigzag Slats and Pinball Clusters
                generateZigzagBricks(list, count = 4, startY = 0.28f, stepY = 0.10f)
                generateClusterField(list, rows = 3, cols = 6, startY = 0.66f)
            }
            5 -> {
                // Solar Corona / Radiant Flares
                generateRings(list, count1 = 12, r1 = 0.18f, count2 = 18, r2 = 0.32f, cx = 0.5f, cy = 0.42f)
                for (angleIdx in 0 until 8) {
                    val ang = (angleIdx * PI * 2 / 8).toFloat()
                    val px = 0.5f + cos(ang) * 0.42f
                    val py = 0.42f + sin(ang) * 0.26f
                    if (py in 0.25f..0.80f && px in 0.10f..0.90f) {
                        list.add(PegTemplate(px, py, width = 15f, height = 15f))
                    }
                }
                generateBottomHopper(list, count = 7, y = 0.76f)
            }
            6 -> {
                // Diamond / Rhombus Layout
                generateDiamond(list, perimeterPoints = 20, cx = 0.5f, cy = 0.45f, rx = 0.34f, ry = 0.24f)
                generateCenterStar(list, cx = 0.5f, cy = 0.45f)
                list.add(PegTemplate(0.16f, 0.45f, shape = PegShape.BRICK, width = 38f, height = 13f, rotation = 90f))
                list.add(PegTemplate(0.84f, 0.45f, shape = PegShape.BRICK, width = 38f, height = 13f, rotation = 90f))
                generateBottomHopper(list, count = 8, y = 0.76f)
            }
            7 -> {
                // Volcano / Caldera Dense Eruption
                generatePyramid(list, rows = 6, topY = 0.28f, stepY = 0.06f, startCount = 3)
                list.add(PegTemplate(0.28f, 0.68f, shape = PegShape.BRICK, width = 44f, height = 14f, rotation = -25f))
                list.add(PegTemplate(0.72f, 0.68f, shape = PegShape.BRICK, width = 44f, height = 14f, rotation = 25f))
                generateBottomHopper(list, count = 7, y = 0.78f)
            }
            8 -> {
                // Dual Sinusoidal Waves
                generateSineWaves(list, waveCount = 2, pointsPerWave = 14, startY = 0.35f, amp = 0.08f)
                generateClusterField(list, rows = 2, cols = 8, startY = 0.66f)
            }
            9 -> {
                // Hourglass / Funnel
                generateHourglass(list, topY = 0.26f, waistY = 0.46f, bottomY = 0.66f)
                generateBottomHopper(list, count = 8, y = 0.76f)
            }
            10 -> {
                // Holy Cross / Cruz de Batalla
                for (i in -4..4) {
                    list.add(PegTemplate(0.5f, 0.45f + i * 0.05f, width = 15f, height = 15f))
                }
                for (j in -4..4) {
                    if (j != 0) {
                        list.add(PegTemplate(0.5f + j * 0.08f, 0.45f, width = 15f, height = 15f))
                    }
                }
                list.add(PegTemplate(0.25f, 0.32f, shape = PegShape.BRICK, width = 44f, height = 13f, rotation = 30f))
                list.add(PegTemplate(0.75f, 0.32f, shape = PegShape.BRICK, width = 44f, height = 13f, rotation = -30f))
                generateBottomHopper(list, count = 8, y = 0.76f)
            }
            11 -> {
                // Hexagonal Honeycomb Matrix
                generateHexHoneycomb(list, cx = 0.5f, cy = 0.45f, rings = 3, radiusStep = 0.10f)
                generateBottomHopper(list, count = 7, y = 0.78f)
            }
            12 -> {
                // Triple Planetary Orbit
                generateRings(list, count1 = 8, r1 = 0.12f, count2 = 14, r2 = 0.24f, cx = 0.5f, cy = 0.44f)
                for (i in 0 until 18) {
                    val ang = (i * PI * 2 / 18).toFloat()
                    list.add(PegTemplate(0.5f + cos(ang) * 0.36f, 0.44f + sin(ang) * 0.28f, width = 14f, height = 14f))
                }
                generateBottomHopper(list, count = 8, y = 0.76f)
            }
            13 -> {
                // Chevron Arrowhead Cascades
                for (c in 0 until 4) {
                    val y = 0.26f + c * 0.11f
                    for (step in 0..3) {
                        val offset = step * 0.07f
                        val drop = step * 0.035f
                        list.add(PegTemplate(0.5f - offset, y + drop, width = 14f, height = 14f))
                        if (step > 0) {
                            list.add(PegTemplate(0.5f + offset, y + drop, width = 14f, height = 14f))
                        }
                    }
                }
                generateBottomHopper(list, count = 8, y = 0.76f)
            }
            14 -> {
                // Pinball Bumper Arena
                for (r in 0 until 5) {
                    val y = 0.28f + r * 0.08f
                    val count = if (r % 2 == 0) 5 else 6
                    val startX = 0.5f - (count - 1) * 0.06f
                    for (c in 0 until count) {
                        list.add(PegTemplate(startX + c * 0.12f, y, width = 15f, height = 15f))
                    }
                }
                list.add(PegTemplate(0.20f, 0.54f, shape = PegShape.BRICK, width = 46f, height = 13f, rotation = 45f))
                list.add(PegTemplate(0.80f, 0.54f, shape = PegShape.BRICK, width = 46f, height = 13f, rotation = -45f))
                generateBottomHopper(list, count = 8, y = 0.77f)
            }
            15 -> {
                // Twin Cascading Waterfalls
                for (i in 0 until 10) {
                    val y = 0.28f + i * 0.045f
                    val wave = sin(i * 0.8f).toFloat() * 0.05f
                    list.add(PegTemplate(0.32f + wave, y, width = 14f, height = 14f))
                    list.add(PegTemplate(0.68f - wave, y, width = 14f, height = 14f))
                }
                generateCenterStar(list, cx = 0.5f, cy = 0.48f)
                generateBottomHopper(list, count = 8, y = 0.77f)
            }
            16 -> {
                // Horseshoe Magnetic Arena
                for (i in 0 until 18) {
                    val ang = (PI * (0.0f + (i.toFloat() / 17f) * 1.0f)).toFloat()
                    val px = 0.5f + cos(ang) * 0.34f
                    val py = 0.55f - sin(ang) * 0.24f
                    list.add(PegTemplate(px, py, width = 14f, height = 14f))
                }
                generateClusterField(list, rows = 3, cols = 5, startY = 0.35f)
                generateBottomHopper(list, count = 7, y = 0.76f)
            }
            17 -> {
                // Infinite Möbius Ribbon (Figure 8)
                for (i in 0 until 28) {
                    val t = (i * PI * 2 / 28).toFloat()
                    val scale = 0.32f
                    val px = 0.5f + (scale * cos(t)) / (1f + sin(t) * sin(t))
                    val py = 0.46f + (scale * sin(t) * cos(t)) / (1f + sin(t) * sin(t))
                    list.add(PegTemplate(px, py, width = 14f, height = 14f))
                }
                generateBottomHopper(list, count = 8, y = 0.76f)
            }
            18 -> {
                // Stepped Citadel / Fortress Pyramid
                generatePyramid(list, rows = 5, topY = 0.26f, stepY = 0.06f, startCount = 2)
                list.add(PegTemplate(0.18f, 0.48f, shape = PegShape.BRICK, width = 42f, height = 13f, rotation = 0f))
                list.add(PegTemplate(0.82f, 0.48f, shape = PegShape.BRICK, width = 42f, height = 13f, rotation = 0f))
                generateClusterField(list, rows = 3, cols = 7, startY = 0.60f)
                generateBottomHopper(list, count = 8, y = 0.78f)
            }
            else -> {
                // Cosmic Starburst
                generateCenterStar(list, cx = 0.5f, cy = 0.44f)
                for (arm in 0 until 8) {
                    val ang = (arm * PI * 2 / 8).toFloat()
                    for (step in 1..3) {
                        val px = 0.5f + cos(ang) * (0.08f + step * 0.09f)
                        val py = 0.44f + sin(ang) * (0.07f + step * 0.075f)
                        if (px in 0.10f..0.90f && py in 0.22f..0.76f) {
                            list.add(PegTemplate(px, py, width = 14f, height = 14f))
                        }
                    }
                }
                generateBottomHopper(list, count = 8, y = 0.76f)
            }
        }

        // Add additional perimeter guard pegs if needed to guarantee minimum 38 pegs
        if (list.size < 38) {
            val needed = 38 - list.size
            for (i in 0 until needed) {
                val frac = i.toFloat() / needed
                val x = 0.15f + frac * 0.70f
                val y = 0.24f + (i % 2) * 0.04f
                list.add(PegTemplate(x, y, width = 14f, height = 14f))
            }
        }

        return list
    }

    // --- Helper Layout Generators ---

    private fun generateHexHoneycomb(list: MutableList<PegTemplate>, cx: Float, cy: Float, rings: Int, radiusStep: Float) {
        list.add(PegTemplate(cx, cy, width = 15f, height = 15f))
        for (r in 1..rings) {
            val count = r * 6
            for (i in 0 until count) {
                val ang = (i * PI * 2 / count).toFloat()
                val px = cx + cos(ang) * (r * radiusStep)
                val py = cy + sin(ang) * (r * radiusStep * 0.85f)
                if (px in 0.12f..0.88f && py in 0.24f..0.76f) {
                    list.add(PegTemplate(px, py, width = 14f, height = 14f))
                }
            }
        }
    }

    private fun generateRings(list: MutableList<PegTemplate>, count1: Int, r1: Float, count2: Int, r2: Float, cx: Float, cy: Float) {
        for (i in 0 until count1) {
            val ang = (i * PI * 2 / count1).toFloat()
            val px = cx + cos(ang) * r1
            val py = cy + sin(ang) * (r1 * 0.75f)
            list.add(PegTemplate(px, py, width = 14f, height = 14f))
        }
        for (i in 0 until count2) {
            val ang = (i * PI * 2 / count2).toFloat()
            val px = cx + cos(ang) * r2
            val py = cy + sin(ang) * (r2 * 0.75f)
            list.add(PegTemplate(px, py, width = 14f, height = 14f))
        }
    }

    private fun generateSpiral(list: MutableList<PegTemplate>, turns: Float, points: Int, maxRadius: Float, cx: Float, cy: Float) {
        for (i in 0 until points) {
            val t = i.toFloat() / points
            val angle = (t * turns * PI * 2).toFloat()
            val r = 0.06f + t * maxRadius
            val px = cx + cos(angle) * r
            val py = cy + sin(angle) * (r * 0.75f)
            list.add(PegTemplate(px, py, width = 14f, height = 14f))
        }
    }

    private fun generateGridColumns(list: MutableList<PegTemplate>, cols: Int, rows: Int, startY: Float, spacingY: Float) {
        for (r in 0 until rows) {
            val y = startY + r * spacingY
            val offset = if (r % 2 == 1) 0.05f else 0f
            for (c in 0 until cols) {
                val x = 0.20f + c * 0.10f + offset
                if (x <= 0.85f) {
                    list.add(PegTemplate(x, y, width = 14f, height = 14f))
                }
            }
        }
    }

    private fun generateFanArcs(list: MutableList<PegTemplate>, arcs: Int, pegsPerArc: Int, startRadius: Float, step: Float) {
        val cx = 0.5f
        val cy = 0.38f
        for (a in 0 until arcs) {
            val r = startRadius + a * step
            for (p in 0 until pegsPerArc) {
                val frac = p.toFloat() / (pegsPerArc - 1)
                val angle = (PI * (0.15f + frac * 0.70f)).toFloat()
                val px = cx + cos(angle) * r
                val py = cy + sin(angle) * (r * 0.8f)
                list.add(PegTemplate(px, py, width = 14f, height = 14f))
            }
        }
    }

    private fun generateZigzagBricks(list: MutableList<PegTemplate>, count: Int, startY: Float, stepY: Float) {
        for (i in 0 until count) {
            val y = startY + i * stepY
            val isLeft = i % 2 == 0
            val x = if (isLeft) 0.30f else 0.70f
            val rot = if (isLeft) 25f else -25f
            list.add(PegTemplate(x, y, shape = PegShape.BRICK, width = 52f, height = 14f, rotation = rot))
            list.add(PegTemplate(x - 0.09f, y - 0.03f, width = 13f, height = 13f))
            list.add(PegTemplate(x + 0.09f, y - 0.03f, width = 13f, height = 13f))
        }
    }

    private fun generateDiamond(list: MutableList<PegTemplate>, perimeterPoints: Int, cx: Float, cy: Float, rx: Float, ry: Float) {
        val corners = listOf(
            Pair(cx, cy - ry),
            Pair(cx + rx, cy),
            Pair(cx, cy + ry),
            Pair(cx - rx, cy)
        )
        val segPoints = perimeterPoints / 4
        for (s in 0 until 4) {
            val p1 = corners[s]
            val p2 = corners[(s + 1) % 4]
            for (k in 0 until segPoints) {
                val t = k.toFloat() / segPoints
                val px = p1.first + (p2.first - p1.first) * t
                val py = p1.second + (p2.second - p1.second) * t
                list.add(PegTemplate(px, py, width = 14f, height = 14f))
            }
        }
    }

    private fun generateCenterStar(list: MutableList<PegTemplate>, cx: Float, cy: Float) {
        list.add(PegTemplate(cx, cy, width = 17f, height = 17f))
        for (i in 0 until 6) {
            val ang = (i * PI * 2 / 6).toFloat()
            val px = cx + cos(ang) * 0.10f
            val py = cy + sin(ang) * 0.08f
            list.add(PegTemplate(px, py, width = 14f, height = 14f))
        }
    }

    private fun generatePyramid(list: MutableList<PegTemplate>, rows: Int, topY: Float, stepY: Float, startCount: Int) {
        for (r in 0 until rows) {
            val y = topY + r * stepY
            val count = startCount + r
            val startX = 0.5f - (count - 1) * 0.045f
            for (c in 0 until count) {
                val x = startX + c * 0.09f
                list.add(PegTemplate(x, y, width = 14f, height = 14f))
            }
        }
    }

    private fun generateSineWaves(list: MutableList<PegTemplate>, waveCount: Int, pointsPerWave: Int, startY: Float, amp: Float) {
        for (w in 0 until waveCount) {
            val baseY = startY + w * 0.14f
            for (p in 0 until pointsPerWave) {
                val frac = p.toFloat() / (pointsPerWave - 1)
                val x = 0.18f + frac * 0.64f
                val y = baseY + sin(frac * PI * 2 * 1.5f).toFloat() * amp
                list.add(PegTemplate(x, y, width = 14f, height = 14f))
            }
        }
    }

    private fun generateHourglass(list: MutableList<PegTemplate>, topY: Float, waistY: Float, bottomY: Float) {
        val topRows = 4
        for (r in 0 until topRows) {
            val frac = r.toFloat() / (topRows - 1)
            val y = topY + frac * (waistY - topY)
            val span = 0.32f * (1f - frac * 0.6f)
            list.add(PegTemplate(0.5f - span, y, width = 14f, height = 14f))
            list.add(PegTemplate(0.5f + span, y, width = 14f, height = 14f))
            if (r % 2 == 0) list.add(PegTemplate(0.5f, y, width = 14f, height = 14f))
        }
        val botRows = 4
        for (r in 0 until botRows) {
            val frac = r.toFloat() / (botRows - 1)
            val y = waistY + frac * (bottomY - waistY)
            val span = 0.12f + frac * 0.20f
            list.add(PegTemplate(0.5f - span, y, width = 14f, height = 14f))
            list.add(PegTemplate(0.5f + span, y, width = 14f, height = 14f))
            if (r % 2 == 1) list.add(PegTemplate(0.5f, y, width = 14f, height = 14f))
        }
    }

    private fun generateClusterField(list: MutableList<PegTemplate>, rows: Int, cols: Int, startY: Float) {
        for (r in 0 until rows) {
            val y = startY + r * 0.05f
            val offset = if (r % 2 == 1) 0.045f else 0f
            for (c in 0 until cols) {
                val x = 0.22f + c * 0.09f + offset
                if (x <= 0.82f) {
                    list.add(PegTemplate(x, y, width = 14f, height = 14f))
                }
            }
        }
    }

    private fun generateBottomHopper(list: MutableList<PegTemplate>, count: Int, y: Float) {
        val startX = 0.5f - (count - 1) * 0.045f
        for (c in 0 until count) {
            val x = startX + c * 0.09f
            list.add(PegTemplate(x, y + (c % 2) * 0.025f, width = 14f, height = 14f))
        }
    }
}
