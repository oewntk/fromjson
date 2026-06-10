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
 * @property inDir input dir
 * @author Bernard Bou
 */
class Factory(
    private val inDir: File,
    val split: Boolean = true,
    val fileext: String = "json",
    val jsonMethod: JsonMethod = JsonMethod.ANY_SERIALIZER,
    val prettyPrint: Boolean = true,
    private val verbose: Boolean = false,
) : Supplier<Model?> {

    val json = JsonCodec(jsonMethod = jsonMethod, prettyPrint = prettyPrint)

    private fun jsonExtra(dir: File): Pair<Collection<VerbFrame>, Collection<VerbTemplate>> {

        val (frameContent, templateContent) =
            if (split) {
                val frameFile = File(dir, "frames.$fileext")
                Tracing.psInfo.printf("[File] %s%n", frameFile)
                val frameContent = frameFile.readText()

                val templateFile = File(dir, "templates.$fileext")
                Tracing.psInfo.printf("[File] %s%n", templateFile)
                val templateContent = templateFile.readText()

                frameContent to templateContent
            } else {
                val frameAndTemplateFile = File(dir, "frames_templates.$fileext")
                Tracing.psInfo.printf("[File] %s%n", frameAndTemplateFile)
                val text = frameAndTemplateFile.readText()
                val content = text.split("\n\n")
                content[0] to content[1]
            }
        val frameMap = safeCast<Map<VerbFrameId, String>>(json.decodeFromString(frameContent))
        val templateMap = safeCast<Map<String, String>>(json.decodeFromString(templateContent))
        val frames = frameMap.entries.map { VerbFrame(it.key, it.value) }.toList()
        val templates = templateMap.entries.map { VerbTemplate(it.key.toInt(), it.value) }.toList()
        return frames to templates
    }

    override fun get(): Model? {
        Tracing.psInfo.println("[Model] $inDir")
        if (!inDir.exists()) {
            throw IllegalArgumentException(inDir.absolutePath)
        }
        val coreModel = CoreFactory(inDir, split = split, fileext = fileext, jsonMethod = jsonMethod).get()
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
}