package com.example.data.model

import java.security.SecureRandom

/**
 * Represents an item in the List Picker with an assigned probability weight.
 * For example, an item with weight 2 is twice as likely to be chosen as an item with weight 1.
 */
data class WeightedListItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val weight: Int = 1
) {
    /**
     * Serializes to string format:
     * - "itemText" if weight is 1 (seamless backward compatibility with plain string lists)
     * - "itemText:::weight" if weight > 1
     */
    fun toSerialized(): String = if (weight == 1) text else "$text:::$weight"

    companion object {
        const val MIN_WEIGHT = 1
        const val MAX_WEIGHT = 99

        /**
         * Parses a serialized string into a WeightedListItem.
         * If the string has no weight marker, default weight of 1 is assigned.
         */
        fun fromSerialized(raw: String): WeightedListItem {
            val trimmed = raw.trim()
            val parts = trimmed.split(":::")
            if (parts.size >= 2) {
                val parsedWeight = parts[1].toIntOrNull()?.coerceIn(MIN_WEIGHT, MAX_WEIGHT) ?: 1
                return WeightedListItem(text = parts[0].trim(), weight = parsedWeight)
            }
            return WeightedListItem(text = trimmed, weight = 1)
        }

        /**
         * Picks items based on relative weights using Cryptographically Secure Random.
         * Probability of selecting item i is weight_i / totalWeight.
         */
        fun sampleWeighted(
            items: List<WeightedListItem>,
            count: Int,
            allowDuplicates: Boolean,
            random: SecureRandom = SecureRandom()
        ): List<WeightedListItem> {
            if (items.isEmpty()) return emptyList()
            val validItems = items.filter { it.text.isNotBlank() }
            if (validItems.isEmpty()) return emptyList()

            val k = count.coerceAtLeast(1)

            fun selectOne(pool: List<WeightedListItem>): WeightedListItem {
                val totalWeight = pool.sumOf { it.weight.coerceIn(MIN_WEIGHT, MAX_WEIGHT) }
                if (totalWeight <= 0) return pool.first()
                val target = random.nextInt(totalWeight)
                var cumulative = 0
                for (item in pool) {
                    cumulative += item.weight.coerceIn(MIN_WEIGHT, MAX_WEIGHT)
                    if (target < cumulative) {
                        return item
                    }
                }
                return pool.last()
            }

            return if (allowDuplicates) {
                List(k) { selectOne(validItems) }
            } else {
                val remaining = validItems.toMutableList()
                val result = mutableListOf<WeightedListItem>()
                val maxPicks = k.coerceAtMost(validItems.size)
                while (result.size < maxPicks && remaining.isNotEmpty()) {
                    val picked = selectOne(remaining)
                    result.add(picked)
                    remaining.remove(picked)
                }
                result
            }
        }
    }
}
