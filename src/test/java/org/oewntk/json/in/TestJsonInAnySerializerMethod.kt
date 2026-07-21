package org.oewntk.json.`in`

import org.junit.Test
import org.oewntk.json.`in`.LibTestData.lex
import org.oewntk.json.`in`.LibTestData.sense
import org.oewntk.json.`in`.LibTestData.synset
import org.oewntk.json.out.JsonCodec
import org.oewntk.json.out.JsonMethod
import org.oewntk.model.*
import org.oewntk.model.Tracing
import java.io.PrintStream

class TestJsonInAnySerializerMethod {

    val json = JsonCodec(jsonMethod = JsonMethod.ANY_SERIALIZER)

    @Test
    fun testDummyLex() {
        val serializable: Map<String, Any> = lex.toData()
        val jsonString = json.encodeToString(serializable)
        ps.println(jsonString)
        val serializable2 = safeCast<Map<String, Any>>(json.decodeFromString(jsonString))
        val lex = lexFromData(serializable2)
        ps.println(lex)
    }

    @Test
    fun testDummySynset() {
        val serializable: Map<String, Any> = synset.toData()
        val jsonString = json.encodeToString(serializable)
        ps.println(jsonString)
        val serializable2 = safeCast<Map<String, Any>>(json.decodeFromString(jsonString))
        val synset = synsetFromData(serializable2)
        ps.println(synset)
    }

    @Test
    fun testDummySense() {
        val serializable: Map<String, Any> = sense.toData()
        val jsonString = json.encodeToString(serializable)
        ps.println(jsonString)
        val serializable2 = safeCast<Map<String, Any>>(json.decodeFromString(jsonString))
        val sense = senseFromData(serializable2)
        ps.println(sense)
    }

    companion object {
        val silent = !System.getProperties().containsKey("VERBOSE") && if (System.getProperties().containsKey("SILENT")) true
        else true

        val ps: PrintStream = if (!silent) Tracing.psInfo else Tracing.psNull

        // @JvmStatic
        // @BeforeClass
        // fun init() {
        // }
    }
}