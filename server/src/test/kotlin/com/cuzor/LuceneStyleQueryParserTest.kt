package com.cuzor

import com.cuzor.rag.retrieval.LuceneStyleQueryParser
import kotlin.test.Test
import kotlin.test.assertEquals

class LuceneStyleQueryParserTest {

    @Test
    fun testBooleanQuery() {

        val query =
            "(hissing OR sound) AND (low OR backup)"

        val parsed =
            LuceneStyleQueryParser.parse(query)

        val terms =
            LuceneStyleQueryParser.extractTerms(parsed)

        assertEquals(
            listOf(
                "hissing",
                "sound",
                "low",
                "backup"
            ),
            terms
        )
    }

    @Test
    fun testSimpleOrQuery() {

        val query =
            "hissing OR sound"

        val parsed =
            LuceneStyleQueryParser.parse(query)

        val terms =
            LuceneStyleQueryParser.extractTerms(parsed)

        assertEquals(
            listOf(
                "hissing",
                "sound"
            ),
            terms
        )
    }

    @Test
    fun testSimpleAndQuery() {

        val query =
            "hissing AND backup"

        val parsed =
            LuceneStyleQueryParser.parse(query)

        val terms =
            LuceneStyleQueryParser.extractTerms(parsed)

        assertEquals(
            listOf(
                "hissing",
                "backup"
            ),
            terms
        )
    }
}