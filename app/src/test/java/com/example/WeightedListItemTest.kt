package com.example

import com.example.data.model.WeightedListItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.SecureRandom

class WeightedListItemTest {

    @Test
    fun testSerializationAndDeserialization() {
        val item1 = WeightedListItem(text = "Apples", weight = 1)
        assertEquals("Apples", item1.toSerialized())
        val parsed1 = WeightedListItem.fromSerialized("Apples")
        assertEquals("Apples", parsed1.text)
        assertEquals(1, parsed1.weight)

        val item2 = WeightedListItem(text = "Oranges", weight = 3)
        assertEquals("Oranges:::3", item2.toSerialized())
        val parsed2 = WeightedListItem.fromSerialized("Oranges:::3")
        assertEquals("Oranges", parsed2.text)
        assertEquals(3, parsed2.weight)
    }

    @Test
    fun testWeightedDistributionRatio() {
        // Item A has weight 2, Item B has weight 1.
        // Item A should be selected ~2/3 of the time (approx. twice as often as B).
        val itemA = WeightedListItem(text = "A", weight = 2)
        val itemB = WeightedListItem(text = "B", weight = 1)
        val list = listOf(itemA, itemB)

        val random = SecureRandom()
        val trials = 6000
        var countA = 0
        var countB = 0

        for (i in 0 until trials) {
            val picked = WeightedListItem.sampleWeighted(list, count = 1, allowDuplicates = true, random = random)
            if (picked.first().text == "A") countA++ else countB++
        }

        // Expected countA ~ 4000, countB ~ 2000. Ratio countA / countB ~ 2.0
        val ratio = countA.toDouble() / countB.toDouble()
        assertTrue("Ratio should be approximately 2.0, was $ratio (A: $countA, B: $countB)", ratio in 1.7..2.3)
    }

    @Test
    fun testSamplingWithoutReplacement() {
        val items = listOf(
            WeightedListItem(text = "A", weight = 5),
            WeightedListItem(text = "B", weight = 1),
            WeightedListItem(text = "C", weight = 1)
        )

        val result = WeightedListItem.sampleWeighted(items, count = 3, allowDuplicates = false)
        assertEquals(3, result.size)
        // All items should be distinct
        assertEquals(3, result.map { it.text }.distinct().size)
    }

    @Test
    fun testClampWeights() {
        val itemTooHigh = WeightedListItem.fromSerialized("Item:::200")
        assertEquals(WeightedListItem.MAX_WEIGHT, itemTooHigh.weight)

        val itemTooLow = WeightedListItem.fromSerialized("Item:::0")
        assertEquals(WeightedListItem.MIN_WEIGHT, itemTooLow.weight)
    }
}
