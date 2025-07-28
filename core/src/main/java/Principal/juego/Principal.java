package Principal.juego;

import Principal.juego.interfaz.MenuPantalla;
import Principal.juego.utiles.Render;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Principal extends Game {
    private SpriteBatch batch;
    private Texture image;

    @Override
    public void create() {
        batch = new SpriteBatch();
        Render.batch = batch;
        Render.app = this;
        this.setScreen(new MenuPantalla()); //pantalla de carga
    }

    @Override
    public void render() {
        super.render(); //para renderizar la pantalla
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
