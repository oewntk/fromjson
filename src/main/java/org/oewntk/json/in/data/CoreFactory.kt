package org.oewntk.json.out.data

import org.oewntk.json.`in`.Tracing
import org.oewntk.json.out.JsonCodec
import org.oewntk.json.out.JsonMethod
import org.oewntk.model.CoreModel
import java.io.File
import java.io.IOException
import java.util.function.Supplier

/**
 * Main class that serializes the core model.
 *
 * @property inDir input dir
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

    private fun jsonCoreModel(dir: File): CoreModel? {

        val (lexContent, synsetContent, senseContent) =
            if (split) {
                var file = File(dir, "oewn-lexes.$fileext")
                Tracing.psInfo.printf("[File] %s%n", file)
                val lexContent = file.readText()

                file = File(dir, "oewn-synsets.$fileext")
                Tracing.psInfo.printf("[File] %s%n", file)
                val synsetContent = file.readText()

                file = File(dir, "oewn-senses.$fileext")
                Tracing.psInfo.printf("[File] %s%n", file)
                val senseContent = file.readText()

                Triple(lexContent, synsetContent, senseContent)
            } else {
                val file = File(dir, "oewn.$fileext")
                Tracing.psInfo.printf("[File] %s%n", file)
                val text = file.readText()
                val content = text.split("\n\n")

                Triple(content[0], content[1], content[2])
            }
        val dataLexes = json.decodeFromString(lexContent)
        val dataSynsets = json.decodeFromString(synsetContent)
        val dataSenses = json.decodeFromString(senseContent)
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
}