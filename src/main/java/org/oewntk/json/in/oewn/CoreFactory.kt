package org.oewntk.json.`in`.oewn

import org.oewntk.json.`in`.Tracing
import org.oewntk.json.out.JsonCodec
import org.oewntk.json.out.JsonMethod
import org.oewntk.model.*
import java.io.File
import java.io.IOException
import java.util.function.Supplier

/**
 * Main class that serializes the core model.
 *
 * @property inDir output dir
 * @author Bernard Bou
 */
class CoreFactory(
    private val inDir: File,
    val split: Boolean = true,
    val fileext: String = "json",
    jsonMethod: JsonMethod = JsonMethod.ANY_SERIALIZER,
    private val verbose: Boolean = true,
) : Supplier<CoreModel?> {

    val json = JsonCodec(jsonMethod = jsonMethod)

    private fun jsonCoreModel(inDir: File): CoreModel? {
        return if (split) {
            val entryFiles = inDir.listFiles { f: File -> f.name.matches("entries.*\\.$fileext".toRegex()) }!!

            // lexes and senses
            val lexesAndSenses = entryFiles.map { file ->
                if (verbose) Tracing.psInfo.printf("[File] %s%n", file)
                val content = file.readText()
                val topDict = safeCast<Map<Lemma, Map<Key2, Map<String, Any>>>>(json.decodeFromString(content))
                lexesAndSensesFromOEWNData(topDict)
            }
            val allLexes = lexesAndSenses.asSequence().flatMap { it.first }
            val allSenses = lexesAndSenses.asSequence().flatMap { it.second }

            // synsets
            val synsetFiles = inDir.listFiles { f: File -> f.name.matches("(noun|verb|adj|adv).*\\.$fileext".toRegex()) }!!
            val synsets = synsetFiles.map { file ->
                if (verbose) Tracing.psInfo.printf("[File] %s%n", file)
                val content = file.readText()
                val topDict = safeCast<Map<SynsetId, Any>>(json.decodeFromString(content))
                topDict.asSequence().map {
                    val synsetDict = safeCast<Map<String, Any>>(it.value)
                    synsetFromOEWNData(it.key, synsetDict)
                }
            }
            val allSynsets = synsets.asSequence().flatten()

            // model
            CoreModel(allLexes.toList(), allSenses.toList(), allSynsets.toList())

        } else {
            val file = File(inDir, "oewn.$fileext")
            if (verbose) Tracing.psInfo.printf("[File] %s%n", file)
            val content = file.readText().split("\n\n")
            val lexTopDict = safeCast<Map<Lemma, Map<Key2, Map<String, Any>>>>(json.decodeFromString(content[0]))
            val (allLexes, allSenses) = lexesAndSensesFromOEWNData(lexTopDict)
            val dataTopDict = safeCast<Map<SynsetId, Any>>(json.decodeFromString(content[1]))
            val allSynsets = dataTopDict.asSequence().map {
                val synsetDict = safeCast<Map<String, Any>>(it.value)
                synsetFromOEWNData(it.key, synsetDict)
            }

            // model
            CoreModel(allLexes.toList(), allSenses.toList(), allSynsets.toList())
        }
    }

    override fun get(): CoreModel? {
        Tracing.psInfo.printf("[CoreModel] %s%n", inDir)
        if (!inDir.exists()) {
            throw IllegalArgumentException(inDir.absolutePath)
        }
        try {
            return jsonCoreModel(inDir)
        } catch (e: IOException) {
            e.printStackTrace(Tracing.psErr)
        }
        return null
    }

    companion object {

        /**
         * Make core model from JSON files
         *
         * @param args command-line arguments
         * @return core model
         */
        private fun makeCoreModel(args: Array<String>): CoreModel? {
            var iArg = 0
            var fileext = "json"
            var one = false
            var jsonMethod = JsonMethod.ANY_SERIALIZER
            var verbose = false
            if ("--verbose" == args[iArg]) {
                verbose = true
                iArg++
            }
            if ("--json" == args[iArg]) {
                fileext = "json"
                iArg++
            }
            if ("-1" == args[iArg]) {
                one = true
                iArg++
            }
            if ("-ij" == args[iArg]) {
                iArg++
                val arg = args[iArg]
                iArg++
                jsonMethod = when (arg) {
                    "a" -> JsonMethod.ANY_SERIALIZER
                    "v" -> JsonMethod.VALUE_WRAPPER
                    "j" -> JsonMethod.JSON_ELEMENT
                    else -> throw IllegalArgumentException("Illegal serialization $arg")
                }
            }
            val inDir = File(args[iArg])
            return CoreFactory(inDir, split = !one, fileext = fileext, jsonMethod = jsonMethod, verbose = verbose).get()
        }

        /**
         * Main
         *
         * @param args command-line arguments
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val model = makeCoreModel(args)
            Tracing.psInfo.printf("[CoreModel] %s%n%s%n%s%n", model!!.source, model.info(), ModelInfo.counts(model))
        }
    }
}