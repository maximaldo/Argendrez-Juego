package Principal.juego.interfaz;

import Principal.juego.elementos.GestorPiezas;
import Principal.juego.elementos.Pieza;
import Principal.juego.utiles.Recursos;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class JuegoPantalla implements Screen {

    private GestorPiezas gestorPiezas;
    private SpriteBatch batch;
    private Texture fondo;
    private Pieza[][] tableroPiezas;
    private TextureRegion torreBlanca, torreNegra, peonBlanco, peonNegro;
    private OrthographicCamera camara;
    private Viewport viewport;

    @Override
    public void show() {
        camara = new OrthographicCamera();
        viewport = new FitViewport(512, 512, camara);
        batch = new SpriteBatch();
        gestorPiezas = new GestorPiezas();

        fondo = new Texture("tablero.jpg");

        Recursos recursos = new Recursos();
        TextureRegion[][] regiones = TextureRegion.split(recursos.piezas, 64, 64);

        torreBlanca = regiones[0][0];
        peonBlanco = regiones[0][1];
        torreNegra = regiones[1][0];
        peonNegro = regiones[1][1];

        tableroPiezas = new Pieza[8][8];

        tableroPiezas[0][0] = new Pieza(torreNegra, 0, 0);
        tableroPiezas[1][0] = new Pieza(peonNegro, 1, 0);
        tableroPiezas[6][0] = new Pieza(peonBlanco, 6, 0);
        tableroPiezas[7][0] = new Pieza(torreBlanca, 7, 0);

        // Pasamos cámara y viewport a InputJugador para calcular bien las coordenadas
        Gdx.input.setInputProcessor(new InputJugador(tableroPiezas, camara, viewport));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camara.update();
        batch.setProjectionMatrix(camara.combined);

        batch.begin();
        batch.draw(fondo, 0, 0);

        // Si gestorPiezas no es útil aún, podés comentar esta línea para evitar dibujar dos veces
        // gestorPiezas.render(batch);

        for (int fila = 0; fila < 8; fila++) {
            for (int col = 0; col < 8; col++) {
                Pieza pieza = tableroPiezas[fila][col];
                if (pieza != null) {
                    pieza.render(batch);
                }
            }
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        batch.dispose();
        fondo.dispose();
        gestorPiezas.dispose();
    }

    // Getter si querés usar
    public Pieza[][] getTableroPiezas() {
        return tableroPiezas;
    }
}
