package com.example.echofind.ui.components.forScreens

import android.content.Context
import com.example.echofind.data.model.chatbot.PreguntaRespuesta
import com.example.echofind.data.model.chatbot.PreguntasRespuestas
import com.google.gson.Gson
import java.io.InputStreamReader

fun cargarPreguntasRespuestas(context: Context): List<PreguntaRespuesta> {
    val inputStream = context.assets.open("preguntas_respuestas.json")
    val reader = InputStreamReader(inputStream)
    val preguntasRespuestas = Gson().fromJson(reader, PreguntasRespuestas::class.java)
    reader.close()
    return preguntasRespuestas.faq
}
