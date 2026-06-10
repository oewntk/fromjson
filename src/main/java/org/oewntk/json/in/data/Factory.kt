package org.oewntk.json.`in`.data

import org.oewntk.json.`in`.Tracing
import org.oewntk.json.out.JsonCodec
import org.oewntk.json.out.JsonMethod
import org.oewntk.model.DataModel
import org.oewntk.model.Model
import org.oewntk.model.VerbFrame
import org.oewntk.model.VerbTemplate
import org.oewntk.model.safeCast
import java.io.File
import java.io.IOException
import java.util.function.Supplier

/**
 * Main class that serializes the core model.
 *
 * @property inDir output dir
 * @author Bernard Bou
 */
class Factory(
    private val inDir: File,
    val split: Boolean = true,
    val fileext: String = "json",
    val jsonMethod: JsonMethod = JsonMethod.ANY_SERIALIZER,
    private val verbose: Boolean = false,
) : Supplier<Model?> {

    val json = JsonCodec(jsonMethod = jsonMethod)

    private fun jsonExtra(dir: File): Pair<Collection<VerbFrame>, Collection<VerbTemplate>> {

        val (frameContent, templateContent) =
            if (split) {
                val frameFile = File(dir, "oewn-frames.$fileext")
                Tracing.psInfo.printf("[File] %s%n", frameFile)
                val frameContent = frameFile.readText()

                val templateFile = File(dir, "oewn-templates.$fileext")
                Tracing.psInfo.printf("[File] %s%n", templateFile)
                val templateContent = templateFile.readText()

                frameContent to templateContent
            } else {
                val frameAndTemplateFile = File(dir, "oewn-frames_templates.$fileext")
                Tracing.psInfo.printf("[File] %s%n", frameAndTemplateFile)
                val text = frameAndTemplateFile.readText()
                val content = text.split("\n\n")
                content[0] to content[1]
            }
        val frames = json.decodeFromString(frameContent)
        val templates = json.decodeFromString(templateContent)
        return safeCast<Collection<VerbFrame>>(frames) to safeCast<Collection<VerbTemplate>>(templates)
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