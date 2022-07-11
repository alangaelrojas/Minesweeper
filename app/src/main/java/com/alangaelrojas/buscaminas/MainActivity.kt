package com.alangaelrojas.buscaminas

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.alangaelrojas.buscaminas.databinding.ActivityMainBinding

@SuppressLint("ClickableViewAccessibility")
class MainActivity : AppCompatActivity(), OnTouchListener {

    private val box: Array<Box?> = Array(1) { null }
    private val board: Tablero by lazy { Tablero(this) }
    private val noOfBombs: TextView get() = binding.tvNoBombs

    private lateinit var binding: ActivityMainBinding
    private var casillas: Array<Array<Box?>> = Array(8) { box }
    private var isActive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        val root = findViewById<LinearLayout>(R.id.root)
        binding = ActivityMainBinding.inflate(layoutInflater, root, false)
        setContentView(binding.root)

        initViews()

        reiniciar()
        supportActionBar?.hide()
    }

    private fun initViews() {

        val layout = binding.lyTablero
        board.setOnTouchListener(this)
        layout.addView(board)

        binding.btnReiniciar.setOnClickListener {
            reiniciar()
        }

        binding.btnInformation.setOnClickListener {
            info()
        }

    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (isActive) for (f in 0..7) {
            for (c in 0..7) {
                if (casillas[f][c]!!.isInside(
                        event.x.toInt(),
                        event.y.toInt()
                    )
                ) {
                    casillas[f][c]!!.isUncovered = true
                    if (casillas[f][c]!!.content == 80) {
                        perdiste()
                    } else if (casillas[f][c]!!.content == 0) recorrer(f, c)
                    board.invalidate()
                }
            }
        }
        if (gano() && isActive) {
            ganaste()
        }
        return true
    }

    internal inner class Tablero(context: Context?) : View(context) {
        override fun onDraw(canvas: Canvas) {
            canvas.drawRGB(0, 0, 0)
            var ancho = 0
            ancho = if (width < height) board.width else board.height
            val anchocua = ancho / 8
            val paint = Paint()
            paint.textSize = 50f
            val paint2 = Paint()
            paint2.textSize = 50f
            paint2.typeface = Typeface.DEFAULT_BOLD
            paint2.setARGB(255, 0, 0, 255)
            val paintlinea1 = Paint()
            paintlinea1.setARGB(255, 255, 255, 255)
            var filaact = 0
            for (f in 0..7) {
                for (c in 0..7) {
                    casillas[f][c]?.fixXAndY(c * anchocua, filaact, anchocua)
                    if (!casillas[f][c]!!.isUncovered) paint.setARGB(
                        255,
                        60,
                        90,
                        176
                    ) else paint.setARGB(153, 204, 204, 204)
                    canvas.drawRect(
                        (c * anchocua).toFloat(), filaact.toFloat(), (c * anchocua
                                + anchocua - 2).toFloat(), (filaact + anchocua - 2).toFloat(), paint
                    )
                    // linea blanca
                    canvas.drawLine(
                        (c * anchocua).toFloat(), filaact.toFloat(), (c * anchocua
                                + anchocua).toFloat(), filaact.toFloat(), paintlinea1
                    )
                    canvas.drawLine(
                        (c * anchocua + anchocua - 1).toFloat(),
                        filaact.toFloat(),
                        (c
                                * anchocua + anchocua - 1).toFloat(),
                        (filaact + anchocua).toFloat(),
                        paintlinea1
                    )
                    if (casillas[f][c]!!.content in 1..8 && casillas[f][c]!!.isUncovered) {
                        canvas.drawText(
                            casillas[f][c]!!.content.toString(), (c
                                    * anchocua + anchocua / 2 - 8).toFloat(), (
                                    filaact + anchocua / 2).toFloat(), paint2
                        )
                    }
                    if (casillas[f][c]!!.content == 80
                        && casillas[f][c]!!.isUncovered
                    ) {
                        val bomba = Paint()
                        bomba.setARGB(255, 255, 0, 0)
                        canvas.drawCircle(
                            (c * anchocua + anchocua / 2).toFloat(), (
                                    filaact + anchocua / 2).toFloat(), 8f, bomba
                        )
                    }
                }
                filaact += anchocua
            }
        }
    }

    private fun disponerBombas() {
        var cantidad = 8
        do {
            val fila = (Math.random() * 8).toInt()
            val columna = (Math.random() * 8).toInt()
            if (casillas[fila][columna]!!.content == 0) {
                casillas[fila][columna]!!.content = 80
                cantidad--
            }
        } while (cantidad != 0)
    }

    private fun contarBombasPerimetro() {
        for (f in 0..7) {
            for (c in 0..7) {
                if (casillas[f][c]!!.content == 0) {
                    casillas[f][c]!!.content = contarCoordenada(f, c)
                }
            }
        }
    }

    private fun recorrer(fil: Int, col: Int) {
        if (fil in 0..7 && col >= 0 && col < 8) {
            if (casillas[fil][col]!!.content == 0) {
                casillas[fil][col]!!.isUncovered = true
                casillas[fil][col]!!.content = 50
                recorrer(fil, col + 1)
                recorrer(fil, col - 1)
                recorrer(fil + 1, col)
                recorrer(fil - 1, col)
                recorrer(fil - 1, col - 1)
                recorrer(fil - 1, col + 1)
                recorrer(fil + 1, col + 1)
                recorrer(fil + 1, col - 1)
            } else {
                if (casillas[fil][col]!!.content in 1..8
                ) {
                    casillas[fil][col]!!.isUncovered = true
                }
            }
        }
    }

    private fun gano(): Boolean {
        var cant = 0
        for (f in 0..7) for (c in 0..7) if (casillas[f][c]!!.isUncovered) cant++
        return cant == 56
    }

    private fun contarCoordenada(fila: Int, columna: Int): Int {
        var total = 0
        if (fila - 1 >= 0 && columna - 1 >= 0) {
            if (casillas[fila - 1][columna - 1]!!.content == 80) total++
        }
        if (fila - 1 >= 0) {
            if (casillas[fila - 1][columna]!!.content == 80) total++
        }
        if (fila - 1 >= 0 && columna + 1 < 8) {
            if (casillas[fila - 1][columna + 1]!!.content == 80) total++
        }
        if (columna + 1 < 8) {
            if (casillas[fila][columna + 1]!!.content == 80) total++
        }
        if (fila + 1 < 8 && columna + 1 < 8) {
            if (casillas[fila + 1][columna + 1]!!.content == 80) total++
        }
        if (fila + 1 < 8) {
            if (casillas[fila + 1][columna]!!.content == 80) total++
        }
        if (fila + 1 < 8 && columna - 1 >= 0) {
            if (casillas[fila + 1][columna - 1]!!.content == 80) total++
        }
        if (columna - 1 >= 0) {
            if (casillas[fila][columna - 1]!!.content == 80) total++
        }
        return total
    }

    fun reiniciar() {
        noOfBombs.text = bombs.toString()
        casillas = Array(8) { arrayOfNulls(8) }
        for (f in 0..7) {
            for (c in 0..7) {
                casillas[f][c] = Box()
            }
        }
        disponerBombas()
        contarBombasPerimetro()
        isActive = true
        board.invalidate()
        playEffect(R.raw.game_start)
    }

    fun info() {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_info, null)
        val cerrar = view.findViewById<ImageView>(R.id.imgCloseDialog)
        val dialog = builder.create()
        cerrar.setOnClickListener { dialog.dismiss() }
        dialog.setView(view)
        dialog.show()
    }

    private fun perdiste() {
        dialogoPerdiste()
        isActive = false
        playEffect(R.raw.lose)
    }

    private fun ganaste() {
        dialogoGanaste()
        isActive = false
        playEffect(R.raw.win)
    }

    private fun dialogoGanaste() {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_win, null)
        val reiniciar = view.findViewById<ImageView>(R.id.btnReiniciar)
        val dialog = builder.create()
        dialog.setView(view)
        dialog.show()
        reiniciar.setOnClickListener { v: View? ->
            reiniciar()
            dialog.dismiss()
        }
    }

    private fun dialogoPerdiste() {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_lose, null)
        val reiniciar = view.findViewById<ImageView>(R.id.btnReiniciar)
        val dialog = builder.create()
        dialog.setView(view)
        dialog.show()
        reiniciar.setOnClickListener { v: View? ->
            reiniciar()
            dialog.dismiss()
        }
    }

    private fun playEffect(resId: Int) {
        val mediaPlayer = MediaPlayer.create(this, resId)
        mediaPlayer.start()
    }

    companion object {
        private const val bombs = 8
    }
}