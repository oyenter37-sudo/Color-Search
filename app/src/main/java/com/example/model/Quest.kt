package com.example.model

data class Quest(
    val id: String,
    val titleRu: String,
    val descriptionRu: String,
    val targetCount: Int,
    val rewardXp: Int,
    val iconName: String,
    val evaluateProgress: (history: List<com.example.data.local.FoundColorEntity>, currentStreak: Int) -> Int
)

object QuestCatalog {
    val QUESTS = listOf(
        Quest(
            id = "first_catch",
            titleRu = "Первый трофей",
            descriptionRu = "Найдите свой первый цвет в реальном мире с помощью камеры",
            targetCount = 1,
            rewardXp = 50,
            iconName = "flag",
            evaluateProgress = { history, _ -> history.size.coerceAtMost(1) }
        ),
        Quest(
            id = "streak_three",
            titleRu = "Охота на триаду",
            descriptionRu = "Поймайте 3 цвета подряд за одну охотничью сессию",
            targetCount = 3,
            rewardXp = 150,
            iconName = "bolt",
            evaluateProgress = { _, streak -> streak.coerceAtMost(3) }
        ),
        Quest(
            id = "sniper_accuracy",
            titleRu = "Снайпер оттенков",
            descriptionRu = "Найдите любой оттенок с невероятной точностью более 88%",
            targetCount = 1,
            rewardXp = 200,
            iconName = "track_changes",
            evaluateProgress = { history, _ ->
                if (history.any { it.accuracy >= 88f }) 1 else 0
            }
        ),
        Quest(
            id = "neon_collector",
            titleRu = "Неоновый визионер",
            descriptionRu = "Найдите хотя бы один редкий неоновый цвет",
            targetCount = 1,
            rewardXp = 250,
            iconName = "wb_sunny",
            evaluateProgress = { history, _ ->
                val neonIds = ColorCatalog.ALL_COLORS
                    .filter { it.rarity == ColorRarity.NEON }
                    .map { it.id }
                    .toSet()
                if (history.any { it.colorId in neonIds }) 1 else 0
            }
        ),
        Quest(
            id = "green_nature",
            titleRu = "Дыхание природы",
            descriptionRu = "Соберите 2 различных оттенка зелёного спектра",
            targetCount = 2,
            rewardXp = 180,
            iconName = "eco",
            evaluateProgress = { history, _ ->
                val greens = history.filter { it.category == ColorCategory.GREEN.displayNameRu }
                    .map { it.colorId }
                    .distinct()
                    .size
                greens.coerceAtMost(2)
            }
        ),
        Quest(
            id = "warm_fire",
            titleRu = "Солнечная энергия",
            descriptionRu = "Найдите тёплый оттенок (жёлтый или оранжевый)",
            targetCount = 1,
            rewardXp = 120,
            iconName = "local_fire_department",
            evaluateProgress = { history, _ ->
                val warmFound = history.any {
                    it.category == ColorCategory.YELLOW.displayNameRu ||
                    it.category == ColorCategory.ORANGE.displayNameRu
                }
                if (warmFound) 1 else 0
            }
        ),
        Quest(
            id = "rainbow_spectrum",
            titleRu = "Абсолютный спектр",
            descriptionRu = "Соберите 7 уникальных цветов в вашу коллекцию",
            targetCount = 7,
            rewardXp = 500,
            iconName = "palette",
            evaluateProgress = { history, _ ->
                history.map { it.colorId }.distinct().size.coerceAtMost(7)
            }
        ),
        Quest(
            id = "grandmaster",
            titleRu = "Магистр палитры",
            descriptionRu = "Соберите 12 уникальных цветов в вашу коллекцию",
            targetCount = 12,
            rewardXp = 1000,
            iconName = "military_tech",
            evaluateProgress = { history, _ ->
                history.map { it.colorId }.distinct().size.coerceAtMost(12)
            }
        )
    )
}
