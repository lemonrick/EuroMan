package sk.mistik.euroman

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Rectangle
import java.util.*

class EuroMan : ApplicationAdapter() {
    private var batch: SpriteBatch? = null
    var pozadie: Texture? = null
    lateinit var postavicka: Array<Texture?>
    var stavPostavicky = 0
    var pauza = 0
    var gravitacia = 0.2f
    var spad = 0f
    var postavickaY = 0
    //Rectangle nam sluzia aby sme vedeli zistit kedy doslo ku kontaktu
    // Postavicky s mincou alebo postavicky s bombou
    var postavickaRectangle: Rectangle? = null
    lateinit var hudba: Music

    val minceX = ArrayList<Int>()
    val minceY = ArrayList<Int>()
    var mincaRectangles = ArrayList<Rectangle>()
    var minca: Texture? = null
    var pocetMinci = 0

    var skore = 0
    var font: BitmapFont? = null
    var stavHry = 0
    var dizzy: Texture? = null
    var ready: Texture? = null
    var gameOver: Texture? = null

    var random: Random? = null

    var bombyX = ArrayList<Int>()
    var bombyY = ArrayList<Int>()
    var bombyRectangles = ArrayList<Rectangle>()
    var bomby: Texture? = null
    var pocetBomb = 0

    override fun create() {
        batch = SpriteBatch()
        pozadie = Texture("bg.jpg")
        //chceme docielit pohyb postavicky, máme 6 frejmov
        postavicka = arrayOfNulls<Texture>(6)
        postavicka[0] = Texture("frame-1.png")
        postavicka[1] = Texture("frame-2.png")
        postavicka[2] = Texture("frame-3.png")
        postavicka[3] = Texture("frame-4.png")
        postavicka[4] = Texture("frame-5.png")
        postavicka[5] = Texture("frame-6.png")

        postavickaY = Gdx.graphics.height / 2

        hudba = Gdx.audio.newMusic(Gdx.files.internal("squid.mp3"))
        hudba.setLooping(true)
        hudba.play()

        minca = Texture("minca.png")
        bomby = Texture("bomba.png")
        dizzy = Texture("dizzy-1.png") //to je postavicka pri naraze s bombou
        ready = Texture("ready.png")
        gameOver = Texture("gameover.png")
        random = Random()

        font = BitmapFont()
        font!!.color = Color.WHITE
        font!!.data.setScale(10f)
    }

    fun vyrobMincu() {
        val vyska = random!!.nextFloat() * Gdx.graphics.height
        minceY.add(vyska.toInt())
        minceX.add(Gdx.graphics.width)
    }

    fun vyrobBombu() {
        val vyska = random!!.nextFloat() * Gdx.graphics.height
        bombyY.add(vyska.toInt())
        bombyX.add(Gdx.graphics.width)
    }

    override fun render() {
        batch!!.begin() //zaciatok
        //nakresli pozadie, urcime polohu x,y a vysku, sirku..to roztiahneme
        batch!!.draw(pozadie, 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        if (stavHry == 1) {
            // A HRAJEME

            // BOMBY
            if (pocetBomb < 250) {
                pocetBomb++
            } else {
                pocetBomb = 0
                vyrobBombu()
            }

            bombyRectangles.clear()
            for (i in bombyX.indices) {
                batch!!.draw(bomby, bombyX[i].toFloat(), bombyY[i].toFloat())
                bombyX[i] = bombyX[i] - 8
                bombyRectangles.add(
                    Rectangle(
                        bombyX[i].toFloat(),
                        bombyY[i].toFloat(), bomby!!.width.toFloat(),
                        bomby!!.height.toFloat()
                    )
                )
            }

            // MINCE
            if (pocetMinci < 100) {
                pocetMinci++
            } else {
                pocetMinci = 0
                vyrobMincu()
            }

            mincaRectangles.clear()
            for (i in minceX.indices) {
                batch!!.draw(minca, minceX[i].toFloat(), minceY[i].toFloat())
                minceX[i] = minceX[i] - 4
                mincaRectangles.add(
                    Rectangle(
                        minceX[i].toFloat(),
                        minceY[i].toFloat(), minca!!.width.toFloat(), minca!!.height.toFloat()
                    )
                )
            }

            if(Gdx.input.justTouched()){

                //print("Y:")
                //println(postavickaY)
                //print("height:")
                //println(Gdx.graphics.height) //2028

                //pri dotyku bude postavicka skakat
                //nehceme aby vyskocila z hora
                if (postavickaY + 380 < Gdx.graphics.height) {
                    spad = (-10).toFloat()
                }

            }

            //pauza je na to aby postavicka "nebezala" prirychlo
            if (pauza < 6) {
                pauza++
            } else {
                pauza = 0
                if (stavPostavicky < 5) {
                    stavPostavicky++
                } else {
                    stavPostavicky = 0
                }
            }

            spad += gravitacia
            postavickaY -= spad.toInt()

            if (postavickaY <= 0) {
                //aby nam postavicka nespadla dole z plochy
                postavickaY = 0
            }
        } else if (stavHry == 0){
            // Čakáme na hráča, že začne hru dotykom

            batch!!.draw(ready,(Gdx.graphics.width / 2 - (postavicka[stavPostavicky]?.width?.div(2) ?: 0)).toFloat() + 40, postavickaY.toFloat() - 2 * (postavicka[stavPostavicky]?.width?.div(2) ?: 0))

            if (Gdx.input.justTouched()) {
                stavHry = 1
            }
        } else if (stavHry == 2){
            // GAME OVER = dohrali sme

            batch!!.draw(gameOver,(Gdx.graphics.width / 2 - (postavicka[stavPostavicky]?.width?.div(2) ?: 0)).toFloat() + 40, (Gdx.graphics.height / 2).toFloat())


            if (Gdx.input.justTouched()) {
                //vsetko vynulujeme
                stavHry = 1
                postavickaY = Gdx.graphics.height / 2
                skore = 0
                spad = 0f
                minceX.clear()
                minceY.clear()
                mincaRectangles.clear()
                pocetMinci = 0
                bombyX.clear()
                bombyY.clear()
                bombyRectangles.clear()
                pocetBomb = 0
            }
        }

        if (stavHry == 2) { //naraz do bomby
            batch!!.draw(
                dizzy,
                (Gdx.graphics.width / 2 - (postavicka[stavPostavicky]?.width ?: 0) / 2).toFloat(),
                postavickaY.toFloat()
            )
        } else {
            //tu sa nam vykresluje postavicka...nebola uplne v strede lebo
            //tiež ma nejake px, tak ju bolo treba posunut aby bola na stred
            batch!!.draw(postavicka[stavPostavicky], (Gdx.graphics.width / 2 - (postavicka[stavPostavicky]?.width?.div(2) ?: 0)).toFloat(), postavickaY.toFloat())
        }


        postavickaRectangle = postavicka[stavPostavicky]?.width?.let {
            Rectangle(
                (Gdx.graphics.width / 2 - postavicka[stavPostavicky]!!.width / 2).toFloat(),
                postavickaY.toFloat(),
                it.toFloat(),
                postavicka[stavPostavicky]!!.height.toFloat()
            )
        }

        for (i in mincaRectangles.indices) {
            if (Intersector.overlaps(postavickaRectangle, mincaRectangles[i])) {
                //Gdx.app.log("Minca!", "Náraz!")
                skore++
                mincaRectangles.removeAt(i) //tu aby sa neprekrývala minca s postavickou
                minceX.removeAt(i)
                minceY.removeAt(i)
                break
            }
        }

        for (i in bombyRectangles.indices) {
            if (Intersector.overlaps(postavickaRectangle, bombyRectangles[i])) {
                Gdx.app.log("Bomba!", "Náraz!")
                stavHry = 2
            }
        }

        font!!.draw(batch, skore.toString(), 100f, 200f)

        batch!!.end() //koniec
    }

    override fun dispose() {
        batch!!.dispose()
    }
}