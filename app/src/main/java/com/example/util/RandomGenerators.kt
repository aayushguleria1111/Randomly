package com.example.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.security.SecureRandom
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlin.random.Random

object RandomGenerators {
    private val secureRandom = SecureRandom()

    // -------------------------------------------------------------
    // 1. RANDOM NUMBERS
    // -------------------------------------------------------------
    fun generateNumbers(
        min: Int,
        max: Int,
        count: Int = 1,
        allowDuplicates: Boolean = true
    ): Result<List<Int>> {
        if (min > max) {
            return Result.failure(IllegalArgumentException("Minimum cannot be greater than maximum"))
        }
        val rangeSize = (max.toLong() - min.toLong() + 1)
        if (!allowDuplicates && count > rangeSize) {
            return Result.failure(IllegalArgumentException("Cannot pick $count unique numbers from a range of $rangeSize values"))
        }
        if (count < 1) {
            return Result.failure(IllegalArgumentException("Count must be at least 1"))
        }

        val results = if (allowDuplicates) {
            List(count) {
                if (min == max) min else min + secureRandom.nextInt((max - min + 1))
            }
        } else {
            val available = (min..max).toMutableList()
            val picked = mutableListOf<Int>()
            for (i in 0 until count) {
                val index = secureRandom.nextInt(available.size)
                picked.add(available.removeAt(index))
            }
            picked
        }
        return Result.success(results)
    }

    // -------------------------------------------------------------
    // 2. RANDOM COLOR
    // -------------------------------------------------------------
    enum class ColorMode {
        ALL,
        PASTEL,
        DARK,
        BRIGHT,
        WARM,
        COOL
    }

    data class ColorResult(
        val color: Color,
        val hex: String,
        val rgbText: String,
        val hslText: String,
        val isDark: Boolean
    )

    fun generateColor(mode: ColorMode = ColorMode.ALL): ColorResult {
        val h: Float
        val s: Float
        val l: Float

        when (mode) {
            ColorMode.ALL -> {
                h = secureRandom.nextFloat() * 360f
                s = 0.2f + secureRandom.nextFloat() * 0.8f
                l = 0.15f + secureRandom.nextFloat() * 0.7f
            }
            ColorMode.PASTEL -> {
                h = secureRandom.nextFloat() * 360f
                s = 0.4f + secureRandom.nextFloat() * 0.35f
                l = 0.75f + secureRandom.nextFloat() * 0.15f
            }
            ColorMode.DARK -> {
                h = secureRandom.nextFloat() * 360f
                s = 0.3f + secureRandom.nextFloat() * 0.6f
                l = 0.12f + secureRandom.nextFloat() * 0.22f
            }
            ColorMode.BRIGHT -> {
                h = secureRandom.nextFloat() * 360f
                s = 0.8f + secureRandom.nextFloat() * 0.2f
                l = 0.45f + secureRandom.nextFloat() * 0.25f
            }
            ColorMode.WARM -> {
                val isRedOrange = secureRandom.nextBoolean()
                h = if (isRedOrange) secureRandom.nextFloat() * 60f else (330f + secureRandom.nextFloat() * 30f)
                s = 0.6f + secureRandom.nextFloat() * 0.4f
                l = 0.4f + secureRandom.nextFloat() * 0.35f
            }
            ColorMode.COOL -> {
                h = 120f + secureRandom.nextFloat() * 160f
                s = 0.6f + secureRandom.nextFloat() * 0.4f
                l = 0.4f + secureRandom.nextFloat() * 0.35f
            }
        }

        val rgb = hslToRgb(h, s, l)
        val color = Color(rgb.first, rgb.second, rgb.third)
        val hex = String.format("#%02X%02X%02X", rgb.first, rgb.second, rgb.third)
        val rgbText = "rgb(${rgb.first}, ${rgb.second}, ${rgb.third})"
        val hslText = "hsl(${h.roundToInt()}°, ${(s * 100).roundToInt()}%, ${(l * 100).roundToInt()}%)"

        val luminance = (0.299 * rgb.first + 0.587 * rgb.second + 0.114 * rgb.third) / 255.0
        val isDark = luminance < 0.55

        return ColorResult(color, hex, rgbText, hslText, isDark)
    }

    private fun hslToRgb(h: Float, s: Float, l: Float): Triple<Int, Int, Int> {
        val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
        val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
        val m = l - c / 2f

        val (r, g, b) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        val rf = (r + m).coerceIn(0f, 1f)
        val gf = (g + m).coerceIn(0f, 1f)
        val bf = (b + m).coerceIn(0f, 1f)

        return Triple((rf * 255).roundToInt(), (gf * 255).roundToInt(), (bf * 255).roundToInt())
    }

    // -------------------------------------------------------------
    // 3. DICE ROLLER
    // -------------------------------------------------------------
    enum class DiceType(val sides: Int, val label: String) {
        D4(4, "d4"),
        D6(6, "d6"),
        D8(8, "d8"),
        D10(10, "d10"),
        D12(12, "d12"),
        D20(20, "d20"),
        D100(100, "d100");

        companion object {
            fun fromLabel(label: String): DiceType =
                entries.find { it.label.equals(label, ignoreCase = true) } ?: D6
        }
    }

    data class DiceRollResult(
        val diceType: DiceType,
        val rolls: List<Int>,
        val modifier: Int,
        val total: Int,
        val equation: String
    )

    fun rollDice(diceType: DiceType, count: Int = 1, modifier: Int = 0): DiceRollResult {
        val safeCount = count.coerceIn(1, 100)
        val rolls = List(safeCount) { secureRandom.nextInt(diceType.sides) + 1 }
        val sum = rolls.sum() + modifier

        val rollPart = rolls.joinToString(" + ")
        val equation = when {
            modifier > 0 -> "$rollPart (+$modifier) = $sum"
            modifier < 0 -> "$rollPart ($modifier) = $sum"
            rolls.size > 1 -> "$rollPart = $sum"
            else -> "$sum"
        }

        return DiceRollResult(diceType, rolls, modifier, sum, equation)
    }

    // -------------------------------------------------------------
    // 4. LIST PICKER
    // -------------------------------------------------------------
    fun pickFromList(
        items: List<String>,
        count: Int = 1,
        allowDuplicates: Boolean = false
    ): Result<List<String>> {
        val validItems = items.map { it.trim() }.filter { it.isNotEmpty() }
        if (validItems.isEmpty()) {
            return Result.failure(IllegalArgumentException("List is empty. Add at least one item."))
        }
        if (!allowDuplicates && count > validItems.size) {
            return Result.failure(IllegalArgumentException("Cannot pick $count unique items from a list of ${validItems.size} items."))
        }

        val result = if (allowDuplicates) {
            List(count) { validItems[secureRandom.nextInt(validItems.size)] }
        } else {
            validItems.shuffled(Random(secureRandom.nextLong())).take(count)
        }
        return Result.success(result)
    }

    // -------------------------------------------------------------
    // 5. COIN FLIP
    // -------------------------------------------------------------
    enum class CoinSide(val label: String) {
        HEADS("Heads"),
        TAILS("Tails")
    }

    fun flipCoin(): CoinSide {
        return if (secureRandom.nextBoolean()) CoinSide.HEADS else CoinSide.TAILS
    }

    fun flipCoins(count: Int): List<CoinSide> {
        val safeCount = count.coerceIn(1, 100)
        return List(safeCount) { flipCoin() }
    }

    // -------------------------------------------------------------
    // 6. RANDOM LETTER
    // -------------------------------------------------------------
    enum class LetterCase {
        UPPERCASE,
        LOWERCASE,
        BOTH
    }

    fun generateLetters(
        count: Int = 1,
        case: LetterCase = LetterCase.UPPERCASE,
        allowDuplicates: Boolean = true
    ): Result<List<Char>> {
        val pool = when (case) {
            LetterCase.UPPERCASE -> ('A'..'Z').toList()
            LetterCase.LOWERCASE -> ('a'..'z').toList()
            LetterCase.BOTH -> (('A'..'Z') + ('a'..'z')).toList()
        }

        if (!allowDuplicates && count > pool.size) {
            return Result.failure(IllegalArgumentException("Cannot generate $count unique letters from pool of ${pool.size}"))
        }

        val letters = if (allowDuplicates) {
            List(count) { pool[secureRandom.nextInt(pool.size)] }
        } else {
            pool.shuffled(Random(secureRandom.nextLong())).take(count)
        }
        return Result.success(letters)
    }

    // -------------------------------------------------------------
    // 7. RANDOM STRING / PASSWORD (TEST ONLY)
    // -------------------------------------------------------------
    fun generateString(
        length: Int = 12,
        includeUpper: Boolean = true,
        includeLower: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = false,
        avoidAmbiguous: Boolean = false
    ): Result<String> {
        val upperChars = "ABCDEFGHJKLMNPQRSTUVWXYZ".let { if (avoidAmbiguous) it else it + "IO" }
        val lowerChars = "abcdefghijkmnopqrstuvwxyz".let { if (avoidAmbiguous) it else it + "l" }
        val numberChars = "23456789".let { if (avoidAmbiguous) it else it + "01" }
        val symbolChars = "!@#$%^&*()_+-=[]{}|;:,.<>?"

        val charPool = StringBuilder()
        val guaranteed = mutableListOf<Char>()

        if (includeUpper) {
            charPool.append(upperChars)
            guaranteed.add(upperChars[secureRandom.nextInt(upperChars.length)])
        }
        if (includeLower) {
            charPool.append(lowerChars)
            guaranteed.add(lowerChars[secureRandom.nextInt(lowerChars.length)])
        }
        if (includeNumbers) {
            charPool.append(numberChars)
            guaranteed.add(numberChars[secureRandom.nextInt(numberChars.length)])
        }
        if (includeSymbols) {
            charPool.append(symbolChars)
            guaranteed.add(symbolChars[secureRandom.nextInt(symbolChars.length)])
        }

        if (charPool.isEmpty()) {
            return Result.failure(IllegalArgumentException("Select at least one character type"))
        }

        val poolStr = charPool.toString()
        val remainingCount = (length - guaranteed.size).coerceAtLeast(0)
        val remaining = List(remainingCount) { poolStr[secureRandom.nextInt(poolStr.length)] }

        val allChars = (guaranteed + remaining).take(length).shuffled(Random(secureRandom.nextLong()))
        return Result.success(allChars.joinToString(""))
    }

    // -------------------------------------------------------------
    // 8. RANDOM DATE
    // -------------------------------------------------------------
    fun generateDate(startDate: LocalDate, endDate: LocalDate): Result<LocalDate> {
        if (startDate.isAfter(endDate)) {
            return Result.failure(IllegalArgumentException("Start date must be before or equal to end date"))
        }
        val daysBetween = ChronoUnit.DAYS.between(startDate, endDate)
        val randomDays = if (daysBetween == 0L) 0L else secureRandom.nextLong(daysBetween + 1)
        return Result.success(startDate.plusDays(randomDays))
    }

    // -------------------------------------------------------------
    // 9. RANDOM TIME
    // -------------------------------------------------------------
    fun generateTime(includeSeconds: Boolean = false, format24Hour: Boolean = false): LocalTime {
        val hour = secureRandom.nextInt(24)
        val minute = secureRandom.nextInt(60)
        val second = if (includeSeconds) secureRandom.nextInt(60) else 0
        return LocalTime.of(hour, minute, second)
    }

    fun generateTime(startTime: LocalTime, endTime: LocalTime, includeSeconds: Boolean = false): LocalTime {
        val startSec = startTime.toSecondOfDay()
        val endSec = endTime.toSecondOfDay()

        val selectedSec = if (startSec <= endSec) {
            startSec + secureRandom.nextInt(endSec - startSec + 1)
        } else {
            val totalSpan = (86400 - startSec) + endSec
            val offset = secureRandom.nextInt(totalSpan + 1)
            (startSec + offset) % 86400
        }

        val baseTime = LocalTime.ofSecondOfDay(selectedSec.toLong())
        return if (includeSeconds) baseTime else baseTime.withSecond(0).withNano(0)
    }

    // -------------------------------------------------------------
    // 10. PLAYING CARDS
    // -------------------------------------------------------------
    enum class CardSuit(val symbol: String, val displayName: String, val color: Color) {
        SPADES("♠", "Spades", Color(0xFF1E293B)),
        HEARTS("♥", "Hearts", Color(0xFFEF4444)),
        DIAMONDS("♦", "Diamonds", Color(0xFFDC2626)),
        CLUBS("♣", "Clubs", Color(0xFF1E293B))
    }

    enum class CardRank(val symbol: String, val displayName: String, val value: Int) {
        ACE("A", "Ace", 1),
        TWO("2", "2", 2),
        THREE("3", "3", 3),
        FOUR("4", "4", 4),
        FIVE("5", "5", 5),
        SIX("6", "6", 6),
        SEVEN("7", "7", 7),
        EIGHT("8", "8", 8),
        NINE("9", "9", 9),
        TEN("10", "10", 10),
        JACK("J", "Jack", 11),
        QUEEN("Q", "Queen", 12),
        KING("K", "King", 13)
    }

    data class PlayingCard(
        val suit: CardSuit,
        val rank: CardRank
    ) {
        val displayName: String get() = "${rank.displayName} of ${suit.displayName}"
        val shortName: String get() = "${rank.symbol}${suit.symbol}"
    }

    fun createStandardDeck(): List<PlayingCard> {
        val deck = mutableListOf<PlayingCard>()
        for (suit in CardSuit.entries) {
            for (rank in CardRank.entries) {
                deck.add(PlayingCard(suit, rank))
            }
        }
        return deck
    }

    fun drawCards(count: Int = 1, allowDuplicates: Boolean = false): List<PlayingCard> {
        val safeCount = count.coerceIn(1, 52)
        val deck = createStandardDeck()
        return if (allowDuplicates) {
            List(safeCount) { deck[secureRandom.nextInt(deck.size)] }
        } else {
            deck.shuffled(Random(secureRandom.nextLong())).take(safeCount)
        }
    }

    // -------------------------------------------------------------
    // 11. YES / NO
    // -------------------------------------------------------------
    fun generateYesNo(includeMaybe: Boolean = false): String {
        val roll = secureRandom.nextInt(100)
        return if (includeMaybe && roll in 45..54) {
            "MAYBE"
        } else if (roll < 50) {
            "YES"
        } else {
            "NO"
        }
    }

    // -------------------------------------------------------------
    // 12. CHOICE
    // -------------------------------------------------------------
    fun chooseBetween(optionA: String, optionB: String): Result<String> {
        val a = optionA.trim()
        val b = optionB.trim()
        if (a.isEmpty() || b.isEmpty()) {
            return Result.failure(IllegalArgumentException("Both options must be filled"))
        }
        return Result.success(if (secureRandom.nextBoolean()) a else b)
    }
}
