package Principal.juego.interfaz;

import Principal.juego.utiles.Recursos;
import Principal.juego.utiles.Render;
import com.badlogic.gdx.Screen;

public class JuegoPantalla implements Screen {

    @Override
    public void show() {
        // Ya debería estar todo cargado
    }

    @Override
    public void render(float delta) {
        Render.limpiarPantallaBlanca();
        Render.batch.begin();

        // Dibujar el tablero
        Render.batch.draw(Recursos.tablero, 0, 0, 800, 800);

        // TODO: Dibujar piezas aquí

        Render.batch.end();
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
