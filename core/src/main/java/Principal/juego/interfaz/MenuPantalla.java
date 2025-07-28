package Principal.juego.interfaz;

import Principal.juego.utiles.Render;
import Principal.juego.utiles.Recursos;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MenuPantalla implements Screen {

    private BitmapFont fuenteGrande;
    private BitmapFont fuentePequena;
    private float tiempo;

    @Override
    public void show() {
        Recursos.cargar();

        fuenteGrande = new BitmapFont();
        fuenteGrande.getData().setScale(3);
        fuenteGrande.setColor(Color.WHITE);

        fuentePequena = new BitmapFont();
        fuentePequena.getData().setScale(1.5f);
        fuentePequena.setColor(Color.LIGHT_GRAY);

        tiempo = 0;
    }

    @Override
    public void render(float delta) {
        tiempo += delta;
        Render.limpiarPantalla(0.1f, 0.1f, 0.15f);

        SpriteBatch batch = Render.batch;
        batch.begin();

        // Dibujar logo centrado arriba
        batch.draw(Recursos.logo,
            (800 - Recursos.logo.getWidth()) / 2f,
            550);

        // Título
        String titulo = "ARGENDREZ";
        GlyphLayout layoutTitulo = new GlyphLayout(fuenteGrande, titulo);
        fuenteGrande.draw(batch, layoutTitulo,
            (800 - layoutTitulo.width) / 2,
            480);

        // Subtítulo
        String subtitulo = "El ajedrez mas piola que vas a ver wacho";
        GlyphLayout layoutSub = new GlyphLayout(fuentePequena, subtitulo);
        fuentePequena.draw(batch, layoutSub,
            (800 - layoutSub.width) / 2,
            430);

        // Texto animado de empezar
        if (((int)(tiempo * 2)) % 2 == 0) {
            String presionar = "Presiona ESPACIO para comenzar a jugar";
            GlyphLayout layout = new GlyphLayout(fuentePequena, presionar);
            fuentePequena.draw(batch, layout,
                (800 - layout.width) / 2,
                350);
        }

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            Recursos.musicaMenu.stop(); // aca la musica al empezar el juego
            Render.app.setScreen(new JuegoPantalla());
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        fuenteGrande.dispose();
        fuentePequena.dispose();
        Recursos.liberar();
    }
}
