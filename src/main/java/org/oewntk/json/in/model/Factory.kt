package org.oewntk.json.`in`.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.oewntk.json.`in`.Tracing
import org.oewntk.model.DataModel
import org.oewntk.model.Model
import java.io.File
import java.io.IOException
import java.util.function.Supplier

/**
 * Main class that serializes the model
 *
 * @property file output file
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
}