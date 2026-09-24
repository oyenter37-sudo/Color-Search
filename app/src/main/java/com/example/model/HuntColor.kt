package com.example.model

import androidx.compose.ui.graphics.Color

enum class ColorCategory(val displayNameRu: String) {
    RED("Красные"),
    ORANGE("Оранжевые"),
    YELLOW("Жёлтые"),
    GREEN("Зелёные"),
    CYAN("Бирюзовые"),
    BLUE("Синие"),
    PURPLE("Фиолетовые"),
    SPECIAL("Необычные")
}

enum class ColorRarity(val labelRu: String, val badgeColor: Color) {
    COMMON("Обычный", Color(0xFF9E9E9E)),
    RARE("Редкий", Color(0xFF00B4D8)),
    EPIC("Эпический", Color(0xFF9D4EDD)),
    NEON("Неоновый", Color(0xFFFF007F))
}

data class HuntColor(
    val id: String,
    val nameRu: String,
    val nameEn: String,
    val hex: String,
    val color: Color,
    val category: ColorCategory,
    val rarity: ColorRarity,
    val hintRu: String
) {
    // RGB components (0-255)
    val redInt: Int get() = (color.red * 255).toInt()
    val greenInt: Int get() = (color.green * 255).toInt()
    val blueInt: Int get() = (color.blue * 255).toInt()
}

object ColorCatalog {
    val ALL_COLORS = listOf(
        // Красные / Розовые
        HuntColor(
            id = "neon_coral",
            nameRu = "Неоновый Коралл",
            nameEn = "Neon Coral",
            hex = "#FF4D6D",
            color = Color(0xFFFF4D6D),
            category = ColorCategory.RED,
            rarity = ColorRarity.NEON,
            hintRu = "Ищите яркие маркеры, коралловые кофты или сочные фрукты"
        ),
        HuntColor(
            id = "ruby_sunset",
            nameRu = "Рубиновый Закат",
            nameEn = "Ruby Sunset",
            hex = "#D90429",
            color = Color(0xFFD90429),
            category = ColorCategory.RED,
            rarity = ColorRarity.COMMON,
            hintRu = "Красные книги, чашки, спелые яблоки или красные крышки"
        ),
        HuntColor(
            id = "electric_fuchsia",
            nameRu = "Электрическая Фуксия",
            nameEn = "Electric Fuchsia",
            hex = "#FF007F",
            color = Color(0xFFFF007F),
            category = ColorCategory.RED,
            rarity = ColorRarity.NEON,
            hintRu = "Яркие стикеры, косметика или элементы одежды"
        ),

        // Оранжевые / Золотые
        HuntColor(
            id = "tangerine_burst",
            nameRu = "Мандариновый Взрыв",
            nameEn = "Tangerine Burst",
            hex = "#FF7B00",
            color = Color(0xFFFF7B00),
            category = ColorCategory.ORANGE,
            rarity = ColorRarity.COMMON,
            hintRu = "Мандарины, апельсиновый сок, рыжие блокноты"
        ),
        HuntColor(
            id = "golden_amber",
            nameRu = "Янтарное Золото",
            nameEn = "Golden Amber",
            hex = "#FFB703",
            color = Color(0xFFFFB703),
            category = ColorCategory.ORANGE,
            rarity = ColorRarity.RARE,
            hintRu = "Золотистая упаковка, мёд, желто-оранжевые карандаши"
        ),

        // Жёлтые / Лаймовые
        HuntColor(
            id = "solar_neon",
            nameRu = "Солнечный Неон",
            nameEn = "Solar Neon",
            hex = "#FFEE00",
            color = Color(0xFFFFEE00),
            category = ColorCategory.YELLOW,
            rarity = ColorRarity.NEON,
            hintRu = "Ярко-жёлтые текстовыделители, стикеры или бананы"
        ),
        HuntColor(
            id = "acid_lime",
            nameRu = "Кислотный Лайм",
            nameEn = "Acid Lime",
            hex = "#70E000",
            color = Color(0xFF70E000),
            category = ColorCategory.YELLOW,
            rarity = ColorRarity.EPIC,
            hintRu = "Лайм, теннисный мяч или спортивная экипировка"
        ),

        // Зелёные
        HuntColor(
            id = "mystic_emerald",
            nameRu = "Мистический Изумруд",
            nameEn = "Mystic Emerald",
            hex = "#06D6A0",
            color = Color(0xFF06D6A0),
            category = ColorCategory.GREEN,
            rarity = ColorRarity.RARE,
            hintRu = "Комнатные растения, зелёные чехлы или бутылочки"
        ),
        HuntColor(
            id = "forest_jade",
            nameRu = "Лесной Нефрит",
            nameEn = "Forest Jade",
            hex = "#2D6A4F",
            color = Color(0xFF2D6A4F),
            category = ColorCategory.GREEN,
            rarity = ColorRarity.COMMON,
            hintRu = "Листья деревьев, тёмно-зелёные блокноты, мох"
        ),
        HuntColor(
            id = "cyber_mint",
            nameRu = "Кибернетическая Мята",
            nameEn = "Cyber Mint",
            hex = "#05F1CD",
            color = Color(0xFF05F1CD),
            category = ColorCategory.GREEN,
            rarity = ColorRarity.NEON,
            hintRu = "Мятные жвачки, светлые наушники или светлая посуда"
        ),

        // Бирюзовые / Голубые
        HuntColor(
            id = "electric_cyan",
            nameRu = "Электрический Циан",
            nameEn = "Electric Cyan",
            hex = "#00F5D4",
            color = Color(0xFF00F5D4),
            category = ColorCategory.CYAN,
            rarity = ColorRarity.NEON,
            hintRu = "Циановые маркеры, упаковки косметики или экран монитора"
        ),
        HuntColor(
            id = "arctic_aqua",
            nameRu = "Ледяной Аквамарин",
            nameEn = "Arctic Aqua",
            hex = "#90E0EF",
            color = Color(0xFF90E0EF),
            category = ColorCategory.CYAN,
            rarity = ColorRarity.RARE,
            hintRu = "Светло-голубая бумага, небо за окном или стеклянные бутылки"
        ),

        // Синие / Индиго
        HuntColor(
            id = "sapphire_spark",
            nameRu = "Сапфировый Блик",
            nameEn = "Sapphire Spark",
            hex = "#4361EE",
            color = Color(0xFF4361EE),
            category = ColorCategory.BLUE,
            rarity = ColorRarity.COMMON,
            hintRu = "Синие ручки, джинсы, синие папки для документов"
        ),
        HuntColor(
            id = "deep_indigo",
            nameRu = "Глубокий Индиго",
            nameEn = "Deep Indigo",
            hex = "#3A0CA3",
            color = Color(0xFF3A0CA3),
            category = ColorCategory.BLUE,
            rarity = ColorRarity.EPIC,
            hintRu = "Тёмно-синяя одежда, вечернее небо, обложки книг"
        ),

        // Фиолетовые / Пурпурные
        HuntColor(
            id = "cosmic_violet",
            nameRu = "Космический Ультрафиолет",
            nameEn = "Cosmic Violet",
            hex = "#7209B7",
            color = Color(0xFF7209B7),
            category = ColorCategory.PURPLE,
            rarity = ColorRarity.EPIC,
            hintRu = "Фиолетовые цветы, баклажан, сиреневые блокноты"
        ),
        HuntColor(
            id = "crystal_amethyst",
            nameRu = "Кристальный Аметист",
            nameEn = "Crystal Amethyst",
            hex = "#9D4EDD",
            color = Color(0xFF9D4EDD),
            category = ColorCategory.PURPLE,
            rarity = ColorRarity.RARE,
            hintRu = "Аметистовые аксессуары, сиреневые маркеры, фиолетовые ручки"
        ),
        HuntColor(
            id = "neon_purple",
            nameRu = "Неоновый Пурпур",
            nameEn = "Neon Purple",
            hex = "#B5179E",
            color = Color(0xFFB5179E),
            category = ColorCategory.PURPLE,
            rarity = ColorRarity.NEON,
            hintRu = "Яркие пурпурные стикеры, чехлы телефонов, сумочки"
        ),
        HuntColor(
            id = "lavender_haze",
            nameRu = "Лавандовый Туман",
            nameEn = "Lavender Haze",
            hex = "#C77DFF",
            color = Color(0xFFC77DFF),
            category = ColorCategory.PURPLE,
            rarity = ColorRarity.RARE,
            hintRu = "Пастельная лавандовая одежда, бумага, цветы"
        ),

        // Необычные / Специальные
        HuntColor(
            id = "choco_truffle",
            nameRu = "Шоколадный Трюфель",
            nameEn = "Choco Truffle",
            hex = "#6F1D1B",
            color = Color(0xFF6F1D1B),
            category = ColorCategory.SPECIAL,
            rarity = ColorRarity.EPIC,
            hintRu = "Коричневое дерево мебели, шоколад, кожаные изделия"
        ),
        HuntColor(
            id = "ash_graphite",
            nameRu = "Пепельный Графит",
            nameEn = "Ash Graphite",
            hex = "#343A40",
            color = Color(0xFF343A40),
            category = ColorCategory.SPECIAL,
            rarity = ColorRarity.COMMON,
            hintRu = "Графитовые карандаши, тёмные клавиатуры, камень"
        )
    )

    fun findById(id: String): HuntColor? = ALL_COLORS.find { it.id == id }
    fun getRandom(): HuntColor = ALL_COLORS.random()
}
