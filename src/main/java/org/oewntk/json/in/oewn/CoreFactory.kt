package org.oewntk.json.`in`.oewn

import org.oewntk.json.`in`.Tracing
import org.oewntk.json.out.JsonCodec
import org.oewntk.json.out.JsonMethod
import org.oewntk.model.CoreModel
import org.oewntk.model.ModelInfo
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
    private val verbose: Boolean = false,
) : Supplier<CoreModel?> {

    val json = JsonCodec(jsonMethod = jsonMethod)

    private fun jsonCoreModel(inDir: File): CoreModel? {
        if (split) {
            val files = inDir.listFiles { f: File -> f.name.matches("entries.*\\.$fileext".toRegex()) }!!
            files.forEach { file ->
                Tracing.psInfo.printf("[File] %s%n", file)
                val content = file.readText()
                val serializable = json.encodeToString(content)
            }

        } else {
            val file = File(inDir, "oewn.$fileext")
            val content = file.readText()
            val serializable = json.decodeFromString(content)
            Tracing.psInfo.printf("[File] %s%n", file)
        }
        return null
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
         * Make core model from YAML files
         *
         * @param args command-line arguments
         * @return core model
         */
        private fun makeCoreModel(args: Array<String>): CoreModel? {
            var iArg = 0
            var verbose = false
            if ("--verbose" == args[iArg]) {
                verbose = true
                iArg++
            }
            var split = false
            if ("--split" == args[iArg]) {
                split = true
                iArg++
            }
            var jsonMethod = JsonMethod.ANY_SERIALIZER
            if ("--json" == args[iArg]) {
                iArg++
                val arg = args[iArg]
                iArg++
                jsonMethod = when (arg) {
                    "s" -> JsonMethod.ANY_SERIALIZER
                    "v" -> JsonMethod.VALUE_WRAPPER
                    "j" -> JsonMethod.JSON_ELEMENT
                    else -> JsonMethod.ANY_SERIALIZER
                }
            }
            val inDir = File(args[iArg])
            return CoreFactory(inDir, split = split, jsonMethod = jsonMethod, verbose = verbose).get()
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