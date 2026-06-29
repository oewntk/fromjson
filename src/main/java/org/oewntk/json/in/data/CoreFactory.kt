package org.oewntk.json.`in`.data

import org.oewntk.json.`in`.Tracing
import org.oewntk.json.out.JsonCodec
import org.oewntk.json.out.JsonMethod
import org.oewntk.model.*
import java.io.File
import java.io.IOException
import java.util.function.Supplier

/**
 * Main class that deserializes a core model.
 *
 * @property inDir input dir
 * @author Bernard Bou
 */
class CoreFactory(
    private val inDir: File,
    private val inverses: Boolean = false,
    private val split: Boolean = true,
    private val fileext: String = "json",
    jsonMethod: JsonMethod = JsonMethod.ANY_SERIALIZER,
    private val verbose: Boolean = false,
) : Supplier<CoreModel?> {

    val json = JsonCodec(jsonMethod = jsonMethod)

    private fun jsonCoreModel(dir: File): CoreModel {

        val (lexContent, synsetContent, senseContent) =
            if (split) {
                var file = File(dir, "oewn-lexes.$fileext")
                if (verbose) Tracing.psInfo.printf("[File] %s%n", file)
                val lexContent = file.readText()

                file = File(dir, "oewn-synsets.$fileext")
                if (verbose) Tracing.psInfo.printf("[File] %s%n", file)
                val synsetContent = file.readText()

                file = File(dir, "oewn-senses.$fileext")
                if (verbose) Tracing.psInfo.printf("[File] %s%n", file)
                val senseContent = file.readText()

                Triple(lexContent, synsetContent, senseContent)
            } else {
                val file = File(dir, "oewn.$fileext")
                if (verbose) Tracing.psInfo.printf("[File] %s%n", file)
                val text = file.readText()
                val content = text.split("\n\n")

                Triple(content[0], content[1], content[2])
            }
        val dataLexes = safeCast<List<Map<String, Any>>>(json.decodeFromString(lexContent))
        val lexes = dataLexes.map { lexFromData(it) }.distinct()
        val dataSynsets = safeCast<List<Map<String, Any>>>(json.decodeFromString(synsetContent))
        val synsets = dataSynsets.map { synsetFromData(it) }.distinct()
        val dataSenses = safeCast<List<Map<String, Any>>>(json.decodeFromString(senseContent))
        val senses = dataSenses.map { senseFromData(it) }.distinct()
        return CoreModel(lexes, senses, synsets)
            .apply { if (inverses) generateInverseRelations() }
    }

    override fun get(): CoreModel? {
        if (verbose) Tracing.psInfo.printf("[CoreModel] %s%n", inDir)
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
            var inverses = false
            var verbose = false
            if ("--verbose" == args[iArg]) {
                verbose = true
                iArg++
            }
            if ("--inverses" == args[iArg]) {
                inverses = true
                iArg++
            }
            if ("--ext" == args[iArg]) {
                iArg++
                val arg = args[iArg]
                iArg++
                fileext = arg
            }
            if ("-i1" == args[iArg]) {
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

            return CoreFactory(inDir, inverses = inverses, fileext = fileext, jsonMethod = jsonMethod, split = !one, verbose = verbose).get()
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