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
import Principal.juego.elementos.TipoPieza;

public class JuegoPantalla implements Screen {

    private static final int TAM_VIRTUAL = 800;

    private final float segPorTurno;
    private final boolean modoBonus; // reloj con bonus (EXTRA)
    private final boolean modoExtra; // reglas extra (peón evolucionado, etc.)

    private SpriteBatch batch;
    private GestorPiezas tablero;
    private InputJugador input;
    private Viewport viewport;
    private Hud hud;

    private PromocionPantalla promoPantalla;

    private BitmapFont fontMsg;
    private final GlyphLayout layout = new GlyphLayout();

    public JuegoPantalla(float segPorTurno) { this(segPorTurno, false, false); }
    public JuegoPantalla(float segPorTurno, boolean modoBonus, boolean modoExtra) {
        this.segPorTurno = segPorTurno;
        this.modoBonus = modoBonus;
        this.modoExtra = modoExtra;
    }

    @Override public void show() {
        batch = new SpriteBatch();
        viewport = new FitViewport(TAM_VIRTUAL, TAM_VIRTUAL);
        viewport.apply(true);

        tablero = new GestorPiezas(modoExtra);
        tablero.onResize((int) viewport.getWorldWidth(), (int) viewport.getWorldHeight());

        input = new InputJugador(tablero, viewport);
        Gdx.input.setInputProcessor(new InputMultiplexer(input));

        hud = new Hud(segPorTurno, modoBonus);

        fontMsg = new BitmapFont();
        fontMsg.getData().setScale(1.6f);
    }

    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        tablero.actualizar(delta);

        // BONUS por captura (solo modoBonus y si no terminó)
        if (modoBonus && !tablero.hayJuegoTerminado()) {
            ColorPieza[] captor = new ColorPieza[1];
            TipoPieza capturada = tablero.consumirPiezaCapturadaYReset(captor);
            if (capturada != null && captor[0] != null) {
                int bonus = capturada.bonusSegundos();
                hud.sumarBonus(captor[0], bonus, segPorTurno * 2f);
            }
        }

        // Mundo
        batch.begin(); tablero.dibujar(batch); batch.end();
        input.dibujarOverlay(batch);

        // HUD y timeout por tiempo (EXTRA)
        if (!tablero.hayJuegoTerminado()) {
            hud.update(delta, input);

            if (modoBonus && hud.hayPerdidaPorTiempo()) {
                // perdió el de HUD.getPerdioPorTiempo(); gana el opuesto
                ColorPieza perdio = hud.getPerdioPorTiempo();
                ColorPieza gano   = (perdio == ColorPieza.BLANCO) ? ColorPieza.NEGRO : ColorPieza.BLANCO;
                tablero.finalizarPorTiempo(gano);
            }
        }
        hud.draw();

        // Promoción
        if (tablero.hayPromocionPendiente() && promoPantalla == null) {
            promoPantalla = new PromocionPantalla(tablero.getPromColor(), tipo -> {
                tablero.promocionar(tipo);
                Gdx.input.setInputProcessor(new InputMultiplexer(input));
                promoPantalla.dispose(); promoPantalla = null;
            });
            Gdx.input.setInputProcessor(new InputMultiplexer(promoPantalla.getStage(), input));
        }
        if (promoPantalla != null) { promoPantalla.act(delta); promoPantalla.draw(); }

        // Victoria (incluye por tiempo)
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

    @Override public void resize(int w, int h) {
        viewport.update(w, h, true);
        tablero.onResize((int) viewport.getWorldWidth(), (int) viewport.getWorldHeight());
        hud.resize(w, h);
        if (promoPantalla != null) promoPantalla.getStage().getViewport().update(w, h, true);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        if (batch != null) batch.dispose();
        if (tablero != null) tablero.dispose();
        if (input != null) input.dispose();
        if (hud != null) hud.dispose();
        if (promoPantalla != null) promoPantalla.dispose();
        if (fontMsg != null) fontMsg.dispose();
    }
}
