package org.oewntk.json.`in`.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.oewntk.json.`in`.Tracing
import org.oewntk.model.CoreModel
import org.oewntk.model.DataCoreModel
import org.oewntk.model.ModelInfo
import java.io.File
import java.io.IOException
import java.util.function.Supplier

/**
 * Main class that deserializes a core model.
 *
 * @property file input file
 * @author Bernard Bou
 */
class CoreFactory(
    private val file: File,
    private val inverses: Boolean = false,
    private val verbose: Boolean = false,
) : Supplier<CoreModel?> {

    val json = Json

    private fun deserializeCoreModel(file: File): CoreModel? {
        val jsonString = file.readText()
        val data: DataCoreModel? = json.decodeFromString(jsonString)
        return data?.let {
            CoreModel(it.lexes, it.senses, it.synsets)
                .apply { if (inverses) generateInverseRelations() }
        }
    }

    override fun get(): CoreModel? {
        if (verbose) Tracing.psInfo.printf("[CoreModel] %s%n", file)
        if (!file.exists()) {
            throw IllegalArgumentException(file.absolutePath)
        }
        try {
            return deserializeCoreModel(file)
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
            val inDir = File(args[iArg])
            return CoreFactory(inDir, inverses = inverses, verbose = verbose).get()
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