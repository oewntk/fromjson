package org.oewntk.json.`in`.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.oewntk.json.`in`.Tracing
import org.oewntk.model.DataModel
import org.oewntk.model.Model
import org.oewntk.model.ModelInfo
import java.io.File
import java.io.IOException
import java.util.function.Supplier

/**
 * Main class that deserializes a model.
 *
 * @property file input file
 * @author Bernard Bou
 */
class Factory(
    private val file: File,
    private val verbose: Boolean = false,
) : Supplier<Model?> {

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json

    private fun deserializeModel(file: File): Model? {
        val jsonString = file.readText()
        val data: DataModel? = json.decodeFromString(jsonString)
        return data?.let { Model(it.lexes, it.senses, it.synsets, it.verbFrames, it.verbTemplates) }
    }

    override fun get(): Model? {
        if (verbose) Tracing.psInfo.printf("[Model] %s%n", file)
        if (!file.exists()) {
            throw IllegalArgumentException(file.absolutePath)
        }
        try {
            return deserializeModel(file)
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
            var verbose = false
            if ("--verbose" == args[iArg]) {
                verbose = true
                iArg++
            }
            val inDir = File(args[iArg])
            return Factory(inDir, verbose = verbose).get()
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