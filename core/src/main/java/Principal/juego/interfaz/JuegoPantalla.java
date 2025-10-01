package Principal.juego.interfaz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import Principal.juego.elementos.ColorPieza;
import Principal.juego.elementos.GestorPiezas;

public class JuegoPantalla implements Screen {

    private static final int TAM_VIRTUAL = 800;

    private final float segPorTurno;

    private SpriteBatch batch;
    private GestorPiezas tablero;
    private InputJugador input;
    private Viewport viewport;

    private Hud hud;

    // promoción (renombrado)
    private PromocionPantalla promoPantalla;

    // mensaje fin de juego
    private BitmapFont fontMsg;
    private final GlyphLayout layout = new GlyphLayout();

    public JuegoPantalla(float segPorTurno) {
        this.segPorTurno = segPorTurno;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        viewport = new FitViewport(TAM_VIRTUAL, TAM_VIRTUAL);
        viewport.apply(true);

        tablero = new GestorPiezas();
        tablero.onResize((int) viewport.getWorldWidth(), (int) viewport.getWorldHeight());

        input = new InputJugador(tablero, viewport);
        Gdx.input.setInputProcessor(new InputMultiplexer(input));

        hud = new Hud(segPorTurno);

        fontMsg = new BitmapFont();
        fontMsg.getData().setScale(1.6f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        tablero.actualizar(delta);

        // 1) mundo
        batch.begin();
        tablero.dibujar(batch);
        batch.end();

        // 2) overlay selección/mov
        input.dibujarOverlay(batch);

        // 3) HUD (si terminó, lo dibujo sin actualizar para “congelar” el tiempo)
        if (!tablero.hayJuegoTerminado()) hud.update(delta, input);
        hud.draw();

        // 4) promoción: levantar pantalla si hace falta
        if (tablero.hayPromocionPendiente() && promoPantalla == null) {
            promoPantalla = new PromocionPantalla(tablero.getPromColor(), tipo -> {
                tablero.promocionar(tipo);
                // volver a input normal
                Gdx.input.setInputProcessor(new InputMultiplexer(input));
                // cerrar pantalla
                promoPantalla.dispose();
                promoPantalla = null;
            });
            // la pantalla de promoción recibe primero el input
            Gdx.input.setInputProcessor(new InputMultiplexer(promoPantalla.getStage(), input));
        }
        if (promoPantalla != null) { promoPantalla.act(delta); promoPantalla.draw(); }

        // 5) mensaje de victoria
        if (tablero.hayJuegoTerminado()) {
            String msg = "GANAN " + (tablero.getGanador() == ColorPieza.BLANCO ? "BLANCAS" : "NEGRAS");
            layout.setText(fontMsg, msg);
            batch.begin();
            fontMsg.draw(batch, layout,
                (viewport.getWorldWidth()  - layout.width) / 2f,
                (viewport.getWorldHeight() + layout.height) / 2f);
            batch.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        tablero.onResize((int) viewport.getWorldWidth(), (int) viewport.getWorldHeight());
        hud.resize(width, height);
        if (promoPantalla != null) promoPantalla.getStage().getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (tablero != null) tablero.dispose();
        if (input != null) input.dispose();
        if (hud != null) hud.dispose();
        if (promoPantalla != null) promoPantalla.dispose();
        if (fontMsg != null) fontMsg.dispose();
    }
}
