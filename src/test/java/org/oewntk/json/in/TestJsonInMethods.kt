package org.oewntk.json.`in`

import kotlinx.serialization.SerializationException
import org.junit.Test
import org.oewntk.json.out.JsonCodec
import org.oewntk.json.out.JsonMethod
import org.oewntk.model.*
import org.oewntk.model.Tracing
import java.io.PrintStream

class TestJsonInMethods {

    val dummyTypedLexString = """
{
  "#type": "map",
  "#val": {
    "lemma": {
      "#type": "string",
      "#val": "jest"
    },
    "type": {
      "#type": "char",
      "#val": "v"
    },
    "sense": {
      "#type": "list",
      "#val": [
        {
          "#type": "string",
          "#val": "jest%2:32:00::"
        },
        {
          "#type": "string",
          "#val": "jest%2:29:00::"
        }
      ]
    },
    "pronunciation": {
      "#type": "list",
      "#val": [
        {
          "#type": "map",
          "#val": {
            "value": {
              "#type": "string",
              "#val": "dʒəʊk"
            },
            "variety": {
              "#type": "string",
              "#val": "GB"
            }
          }
        },
        {
          "#type": "map",
          "#val": {
            "value": {
              "#type": "string",
              "#val": "dʒoʊk"
            },
            "variety": {
              "#type": "string",
              "#val": "US"
            }
          }
        }
      ]
    }
  }
}
"""

    val dummyTypedSynsetString = """
{
  "#type": "map",
  "#val": {
    "id": {
      "#type": "string",
      "#val": "00855315-v"
    },
    "type": {
      "#type": "char",
      "#val": "v"
    },
    "domain": {
      "#type": "string",
      "#val": "communication"
    },
    "member": {
      "#type": "list",
      "#val": [
        {
          "#type": "string",
          "#val": "joke"
        },
        {
          "#type": "string",
          "#val": "jest"
        }
      ]
    },
    "definition": {
      "#type": "list",
      "#val": [
        {
          "#type": "string",
          "#val": "tell a joke"
        },
        {
          "#type": "string",
          "#val": "speak humorously"
        }
      ]
    },
    "examples": {
      "#type": "list",
      "#val": [
        {
          "#type": "string",
          "#val": "He often jokes"
        }
      ]
    }
  }
}
"""

    val dummyTypedSenseString = """
{
  "#type": "map",
  "#val": {
    "id": {
      "#type": "string",
      "#val": "jest%2:32:00::"
    },
    "index": {
      "#type": "int",
      "#val": 0
    },
    "synset": {
      "#type": "string",
      "#val": "00855315-v"
    },
    "lemma": {
      "#type": "string",
      "#val": "jest"
    },
    "type": {
      "#type": "char",
      "#val": "v"
    }
  }
}
"""

    val dummyLexString = """
 {
    "lemma": "jest",
    "type": "v",
    "sense": [
      "jest%2:32:00::",
      "jest%2:29:00::"
    ],
    "pronunciation": [
      {
        "value": "dʒəʊk",
        "variety": "GB"
      },
      {
        "value": "dʒoʊk",
        "variety": "US"
      }
    ]
}
"""

    val dummySynsetString = """
{
    "id": "00855315-v",
    "type": "v",
    "domain": "communication",
    "member": [
      "joke",
      "jest"
    ],
    "definition": [
      "tell a joke",
      "speak humorously"
    ],
    "examples": [
      "He often jokes"
    ]
}
"""

    val dummySenseString = """
 {
    "id": "jest%2:32:00::",
    "index": 0,
    "synset": "00855315-v",
    "lemma": "jest",
    "type": "v"
}
"""

    val wrapperString = """{"data": %s}"""

    fun wrap(str: String) = wrapperString.format(str)

    @Test
    fun testDummyLex() {
        val json = JsonCodec(jsonMethod = JsonMethod.ANY_SERIALIZER)
        val serializable = safeCast<Map<String, Any>>(json.decodeFromString(wrap(dummyLexString)))
        val lex = lexFromData(serializable)
        ps.println(lex)
    }

    @Test
    fun testDummySynset() {
        val json = JsonCodec(jsonMethod = JsonMethod.ANY_SERIALIZER)
        val serializable = safeCast<Map<String, Any>>(json.decodeFromString(wrap(dummySynsetString)))
        val synset = synsetFromData(serializable)
        ps.println(synset)
    }

    @Test
    fun testDummySense() {
        val json = JsonCodec(jsonMethod = JsonMethod.ANY_SERIALIZER)
        val serializable = safeCast<Map<String, Any>>(json.decodeFromString(wrap(dummySenseString)))
        val sense = senseFromData(serializable)
        ps.println(sense)
    }

    @Test
    fun testJsonElementDummyLex() {
        val json = JsonCodec(jsonMethod = JsonMethod.JSON_ELEMENT)
        val serializable = safeCast<Map<String, Any>>(json.decodeFromString(dummyLexString))
        val lex = lexFromData(serializable)
        ps.println(lex)
    }

    @Test
    fun testJsonElementDummySynset() {
        val json = JsonCodec(jsonMethod = JsonMethod.JSON_ELEMENT)
        val serializable = safeCast<Map<String, Any>>(json.decodeFromString(dummySynsetString))
        val synset = synsetFromData(serializable)
        ps.println(synset)
    }

    @Test
    fun testJsonElementDummySense() {
        val json = JsonCodec(jsonMethod = JsonMethod.JSON_ELEMENT)
        val serializable = safeCast<Map<String, Any>>(json.decodeFromString(dummySenseString))
        val sense = senseFromData(serializable)
        ps.println(sense)
    }

    @Test(expected = SerializationException::class)
    fun testValueWrapperDummyLex() {
        val json = JsonCodec(jsonMethod = JsonMethod.VALUE_WRAPPER)
        val serializable = safeCast<Map<String, Any>>(json.decodeFromString(wrap(dummyTypedLexString)))
        val lex = lexFromData(serializable)
        ps.println(lex)
    }

    @Test(expected = SerializationException::class)
    fun testValueWrapperDummySynset() {
        val json = JsonCodec(jsonMethod = JsonMethod.VALUE_WRAPPER)
        val serializable = safeCast<Map<String, Any>>(json.decodeFromString(wrap(dummyTypedSynsetString)))
        val synset = synsetFromData(serializable)
        ps.println(synset)
    }

    @Test(expected = SerializationException::class)
    fun testValueWrapperDummySense() {
        val json = JsonCodec(jsonMethod = JsonMethod.VALUE_WRAPPER)
        val serializable = safeCast<Map<String, Any>>(json.decodeFromString(wrap(dummyTypedSenseString)))
        val sense = senseFromData(serializable)
        ps.println(sense)
    }

    companion object {
        val silent = if (System.getProperties().containsKey("VERBOSE")) false
        else if (System.getProperties().containsKey("SILENT")) true
        else true

        val ps: PrintStream = if (!silent) Tracing.psInfo else Tracing.psNull

        // @JvmStatic
        // @BeforeClass
        // fun init() {
        // }
    }
}