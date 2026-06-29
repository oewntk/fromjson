package org.oewntk.json.`in`.data

import org.oewntk.json.`in`.Tracing
import org.oewntk.json.out.JsonCodec
import org.oewntk.json.out.JsonMethod
import org.oewntk.model.*
import java.io.File
import java.io.IOException
import java.util.function.Supplier

/**
 * Main class that deserializes a model.
 *
 * @property inDir output dir
 * @author Bernard Bou
 */
class Factory(
    private val inDir: File,
    private val inverses: Boolean = false,
    private val split: Boolean = true,
    private val fileext: String = "json",
    private val jsonMethod: JsonMethod = JsonMethod.ANY_SERIALIZER,
    private val verbose: Boolean = false,
) : Supplier<Model?> {

    val json = JsonCodec(jsonMethod = jsonMethod)

    private fun jsonExtra(dir: File): Pair<List<VerbFrame>, List<VerbTemplate>> {

        val (frameContent, templateContent) =
            if (split) {
                val frameFile = File(dir, "oewn-frames.$fileext")
                if (verbose) Tracing.psInfo.printf("[File] %s%n", frameFile)
                val frameContent = frameFile.readText()

                val templateFile = File(dir, "oewn-templates.$fileext")
                if (verbose) Tracing.psInfo.printf("[File] %s%n", templateFile)
                val templateContent = templateFile.readText()

                frameContent to templateContent
            } else {
                val frameAndTemplateFile = File(dir, "oewn-frames_templates.$fileext")
                if (verbose) Tracing.psInfo.printf("[File] %s%n", frameAndTemplateFile)
                val text = frameAndTemplateFile.readText()
                val content = text.split("\n\n")
                content[0] to content[1]
            }
        val frames = safeCast<Map<String, String>>(json.decodeFromString(frameContent))
        val templates = safeCast<Map<String, String>>(json.decodeFromString(templateContent))
        return (frames.map { VerbFrame(it.key, it.value) }.toList()) to (templates.map { VerbTemplate(it.key.toInt(), it.value) }.toList())
    }

    override fun get(): Model? {
        if (verbose) Tracing.psInfo.println("[Model] $inDir")
        if (!inDir.exists()) {
            throw IllegalArgumentException(inDir.absolutePath)
        }
        val coreModel = CoreFactory(inDir, inverses= inverses, split = split, fileext = fileext, jsonMethod = jsonMethod).get()
        try {
            val framesAndTemplates = jsonExtra(inDir)
            val (frames, templates) = framesAndTemplates
            val data = DataModel(coreModel!!, frames, templates)
            return Model(data, inDir.absolutePath, inDir.absolutePath)
        } catch (e: IOException) {
            e.printStackTrace(Tracing.psErr)
        }
        return null
    }


    companion object {

        /**
         * Make model from JSON files
         *
         * @param args command-line arguments
         * @return model
         */
        private fun makeModel(args: Array<String>): Model? {
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

            return Factory(inDir, inverses = inverses, fileext = fileext, jsonMethod = jsonMethod, split = !one, verbose = verbose).get()
        }

        /**
         * Main
         *
         * @param args command-line arguments
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val model = makeModel(args)
            Tracing.psInfo.printf("[Model] %s%n%s%n%s%n", model!!.source, model.info(), ModelInfo.counts(model))
        }
    }
}