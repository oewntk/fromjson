package org.oewntk.json.`in`.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.oewntk.json.`in`.Tracing
import org.oewntk.model.CoreModel
import org.oewntk.model.DataModel
import org.oewntk.model.Model
import org.oewntk.model.ModelInfo
import java.io.File
import java.io.IOException
import java.util.function.Supplier

/**
 * Main class that deserializes the model.
 *
 * @property file input file
 * @author Bernard Bou
 */
class Factory(
    private val file: File,
    private val fileext: String = "json",
    private val throws: Boolean = true,
    private val inverses: Boolean = false,
    private val verbose: Boolean = false,
) : Supplier<Model?> {

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json

    private fun deserializeCoreModel(file: File): Model? {
        val jsonString = file.readText()
        val data: DataModel? = json.decodeFromString(jsonString)
        return data?.let { Model(it.lexes, it.senses, it.synsets, it.verbFrames, it.verbTemplates) }
    }

    override fun get(): Model? {
        Tracing.psInfo.printf("[Model] %s%n", file)
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
        private fun makeModel(args: Array<String>): Model? {
            var iArg = 0
            var fileext = "json"
            var verbose = false
            if ("--verbose" == args[iArg]) {
                verbose = true
                iArg++
            }
            if ("--json" == args[iArg]) {
                fileext = "json"
                iArg++
            }
            val inDir = File(args[iArg])
            return Factory(inDir, fileext = fileext, verbose = verbose).get()
        }

        /**
         * Main
         *
         * @param args command-line arguments
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val model = makeModel(args)
            Tracing.psInfo.printf("[CoreModel] %s%n%s%n%s%n", model!!.source, model.info(), ModelInfo.counts(model))
        }
    }
}