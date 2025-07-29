package Principal.juego.elementos;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Pieza {
    private Sprite sprite;
    private int fila;
    private int columna;
    private boolean esBlanca;

    public Pieza(Texture textura, int fila, int columna, boolean esBlanca) {
        this.sprite = new Sprite(textura);
        this.fila = fila;
        this.columna = columna;
        this.esBlanca = esBlanca;
    }
    public void moverA(int nuevaFila, int nuevaColumna) {
        this.fila = nuevaFila;
        this.columna = nuevaColumna;
    }


    public void render(SpriteBatch batch, float tamanioCasilla, float offsetX, float offsetY) {
        float x = offsetX + columna * tamanioCasilla;
        float y = offsetY + fila * tamanioCasilla;
        sprite.setSize(tamanioCasilla, tamanioCasilla);
        sprite.setPosition(x, y);
        sprite.draw(batch);
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    public boolean esBlanca() {
        return esBlanca;
    }
}
