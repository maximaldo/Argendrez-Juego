package Principal.juego.elementos;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Pieza {
    private Sprite sprite;
    private int fila;
    private int columna;

    public Pieza(TextureRegion region, int fila, int columna) {
        this.sprite = new Sprite(region);
        this.fila = fila;
        this.columna = columna;
        actualizarPosicionEnPantalla();
    }

    public void actualizarPosicionEnPantalla() {
        int tileSize = 64;
        sprite.setPosition(columna * tileSize, fila * tileSize);
    }

    public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        sprite.draw(batch);
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    public void moverA(int nuevaFila, int nuevaColumna) {
        this.fila = nuevaFila;
        this.columna = nuevaColumna;
        actualizarPosicionEnPantalla();
    }
}
