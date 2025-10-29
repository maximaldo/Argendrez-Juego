package Principal.juego.interfaz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.function.BiConsumer;

import Principal.juego.elementos.ColorPieza;
import Principal.juego.elementos.GestorPiezas;
import Principal.juego.elementos.Pieza;
import Principal.juego.elementos.TipoPieza;
import Principal.juego.variantes.cartas.ManoCartas;
import Principal.juego.variantes.cartas.Ruleta;
import Principal.juego.variantes.cartas.TipoCarta;

public class JuegoPantalla implements Screen {

    private static final int TAM_VIRTUAL = 800;
    private static final int PANEL_W = 240; // ancho lateral del panel de cartas

    private final float segPorTurno;
    private final boolean modoBonus; // bonus por captura
    private final boolean modoExtra; // peón evo + cartas/ruleta

    private SpriteBatch batch;
    private GestorPiezas tablero;
    private InputJugador input;
    private Viewport viewport;
    private Hud hud;

    private CartasHUD cartasHUD;
    private final Ruleta ruleta = new Ruleta();
    private final ManoCartas manoBlancas = new ManoCartas();
    private final ManoCartas manoNegras  = new ManoCartas();
    private boolean cartaJugadaEsteTurno = false;

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
        // reservar espacio para panel derecho si está el modo extra
        tablero.onResize((int) (viewport.getWorldWidth() - (modoExtra ? PANEL_W : 0)),
            (int) viewport.getWorldHeight());

        input = new InputJugador(tablero, viewport);
        InputMultiplexer mux = new InputMultiplexer(input);
        Gdx.input.setInputProcessor(mux);

        hud = new Hud(segPorTurno, modoBonus);

        if (modoExtra) {
            cartasHUD = new CartasHUD(this::intentarJugarCarta, PANEL_W);
            int restante = (input.getTurno() == ColorPieza.BLANCO)
                ? ruleta.getRestanteBlancas()
                : ruleta.getRestanteNegras();
            cartasHUD.setRuletaRestante(restante);
            cartasHUD.setTurno(input.getTurno());
            cartasHUD.setMano(manoActual().getCartas());
            mux = new InputMultiplexer(cartasHUD.getStage(), input);
            Gdx.input.setInputProcessor(mux);
        }

        fontMsg = new BitmapFont();
        fontMsg.getData().setScale(1.6f);
    }

    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        tablero.actualizar(delta);
        chequearCambioTurno(); // efectos y reparto

        if (modoBonus && !tablero.hayJuegoTerminado()) {
            ColorPieza[] capPor = new ColorPieza[1];
            TipoPieza cap = tablero.consumirPiezaCapturadaYReset(capPor);
            if (cap != null && capPor[0] != null) {
                hud.sumarBonus(capPor[0], cap.bonusSegundos(), segPorTurno * 2f);
            }
        }

        batch.begin();
        tablero.dibujar(batch);
        batch.end();

        input.dibujarOverlay(batch);

        if (!tablero.hayJuegoTerminado()) {
            hud.update(delta, input);
            if (modoBonus && hud.hayPerdidaPorTiempo()) {
                ColorPieza perdio = hud.getPerdioPorTiempo();
                tablero.finalizarPorTiempo(perdio == ColorPieza.BLANCO ? ColorPieza.NEGRO : ColorPieza.BLANCO);
            }
        }
        hud.draw();

        if (modoExtra && cartasHUD != null) {
            cartasHUD.setTurno(input.getTurno());
            cartasHUD.setMano(manoActual().getCartas());
            int restante = (input.getTurno() == ColorPieza.BLANCO)
                ? ruleta.getRestanteBlancas()
                : ruleta.getRestanteNegras();
            cartasHUD.setRuletaRestante(restante);
            cartasHUD.act(delta);
            cartasHUD.draw();
        }

        if (tablero.hayPromocionPendiente() && promoPantalla == null) {
            promoPantalla = new PromocionPantalla(tablero.getPromColor(), tipo -> {
                tablero.promocionar(tipo);
                Gdx.input.setInputProcessor(new InputMultiplexer(
                    (cartasHUD != null ? cartasHUD.getStage() : null), input));
                promoPantalla.dispose();
                promoPantalla = null;
            });
            if (cartasHUD != null)
                Gdx.input.setInputProcessor(new InputMultiplexer(promoPantalla.getStage(), cartasHUD.getStage(), input));
            else
                Gdx.input.setInputProcessor(new InputMultiplexer(promoPantalla.getStage(), input));
        }
        if (promoPantalla != null) { promoPantalla.act(delta); promoPantalla.draw(); }

        if (tablero.hayJuegoTerminado()) {
            String msg = "GANAN " + (tablero.getGanador() == ColorPieza.BLANCO ? "BLANCAS" : "NEGRAS");
            layout.setText(fontMsg, msg);

            // === CENTRADO SOBRE EL TABLERO (no toda la ventana) ===
            float boardW = tablero.getTamCelda() * 8f;
            float cx = tablero.getOrigenX() + (boardW - layout.width) / 2f;
            float cy = tablero.getOrigenY() + (boardW + layout.height) / 2f;

            batch.begin();
            fontMsg.draw(batch, layout, cx, cy);
            batch.end();
        }
    }

    @Override public void resize(int w, int h) {
        viewport.update(w, h, true);
        tablero.onResize((int) (viewport.getWorldWidth() - (modoExtra ? PANEL_W : 0)),
            (int) viewport.getWorldHeight());
        hud.resize(w, h);
        if (promoPantalla != null) promoPantalla.getStage().getViewport().update(w, h, true);
        if (cartasHUD != null) cartasHUD.resize(w, h);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        if (batch != null) batch.dispose();
        if (tablero != null) tablero.dispose();
        if (input != null) input.dispose();
        if (hud != null) hud.dispose();
        if (cartasHUD != null) cartasHUD.dispose();
        if (promoPantalla != null) promoPantalla.dispose();
        if (fontMsg != null) fontMsg.dispose();
    }

    // ====== cartas / ruleta ======
    private ManoCartas manoActual() {
        return (input.getTurno() == ColorPieza.BLANCO) ? manoBlancas : manoNegras;
    }

    private ColorPieza turnoPrevio = null;
    private void chequearCambioTurno() {
        ColorPieza turnoActual = input.getTurno();
        if (turnoPrevio == null) { turnoPrevio = turnoActual; return; }
        if (turnoActual != turnoPrevio) {
            tablero.tickEfectosAlComenzarTurno(turnoActual);

            if (modoExtra) {
                boolean daCarta = (turnoActual == ColorPieza.BLANCO)
                    ? ruleta.tickParaBlancas()
                    : ruleta.tickParaNegras();

                if (daCarta) {
                    ManoCartas manoQueEmpieza = (turnoActual == ColorPieza.BLANCO) ? manoBlancas : manoNegras;
                    if (manoQueEmpieza.puedeRobar()) manoQueEmpieza.robar(ruleta.robarCarta());
                }

                if (cartasHUD != null) {
                    int restante = (turnoActual == ColorPieza.BLANCO)
                        ? ruleta.getRestanteBlancas()
                        : ruleta.getRestanteNegras();
                    cartasHUD.setTurno(turnoActual);
                    cartasHUD.setMano(manoActual().getCartas());
                    cartasHUD.setRuletaRestante(restante);
                }

                cartaJugadaEsteTurno = false;
            }
            turnoPrevio = turnoActual;
        }
    }

    private void intentarJugarCarta(TipoCarta carta) {
        if (!modoExtra || tablero.hayJuegoTerminado() || cartaJugadaEsteTurno) return;

        switch (carta) {
            case PRORROGA:
                hud.sumarBonus(input.getTurno(), 40, segPorTurno * 2f);
                manoActual().gastar(carta);
                cartaJugadaEsteTurno = true;
                break;

            case SPRINT:
                seleccionarPropia((x,y)->{
                    tablero.aplicarSprint(x,y);
                    manoActual().gastar(carta);
                    cartaJugadaEsteTurno = true;
                });
                break;

            case FORTIFICACION:
                seleccionarPropia((x,y)->{
                    tablero.aplicarFortificacion(x,y, input.getTurno());
                    manoActual().gastar(carta);
                    cartaJugadaEsteTurno = true;
                });
                break;

            case CONGELAR:
                seleccionarRival((x,y)->{
                    tablero.aplicarCongelar(x,y, input.getTurno());
                    manoActual().gastar(carta);
                    cartaJugadaEsteTurno = true;
                });
                break;

            case REAGRUPACION:
                seleccionarDosPropiasAdyacentes((x1,y1,x2,y2)->{
                    tablero.intercambiar(x1,y1,x2,y2);
                    manoActual().gastar(carta);
                    cartaJugadaEsteTurno = true;
                });
                break;

            case TELEPEON:
                seleccionarPropia((x,y)->{
                    Pieza p = tablero.obtener(x,y);
                    if (p != null && p.tipo == TipoPieza.PEON && p.color == input.getTurno()) {
                        int dir = p.color.dirPeon();
                        int nx = x, ny = y + dir;
                        if (tablero.enTablero(nx,ny) && tablero.obtener(nx,ny) == null) {
                            tablero.mover(x,y,nx,ny);
                        }
                    }
                    manoActual().gastar(carta);
                    cartaJugadaEsteTurno = true;
                });
                break;
        }
    }

    // ===== helpers selección =====
    private void seleccionarPropia(BiConsumer<Integer,Integer> pick) {
        pushSelector((x,y)->{
            Pieza p = tablero.obtener(x,y);
            if (p != null && p.color == input.getTurno()) pick.accept(x,y);
        });
    }
    private void seleccionarRival(BiConsumer<Integer,Integer> pick) {
        pushSelector((x,y)->{
            Pieza p = tablero.obtener(x,y);
            if (p != null && p.color != input.getTurno()) pick.accept(x,y);
        });
    }
    private boolean sonAdyacentesOrto(int x1, int y1, int x2, int y2){
        return Math.abs(x1 - x2) + Math.abs(y1 - y2) == 1;
    }
    private interface Pick2 { void run(int x1,int y1,int x2,int y2); }

    private void seleccionarDosPropiasAdyacentes(Pick2 pick){
        final int[] sel = {-1,-1};
        final InputAdapter ad = new InputAdapter(){
            @Override public boolean touchDown(int sx,int sy,int pointer,int button){
                Vector2 w = new Vector2(sx,sy);
                viewport.unproject(w);
                int x = (int)((w.x - tablero.getOrigenX())/tablero.getTamCelda());
                int y = (int)((w.y - tablero.getOrigenY())/tablero.getTamCelda());
                if (!tablero.enTablero(x,y)) return true;

                Pieza p = tablero.obtener(x,y);
                if (p == null || p.color != input.getTurno()) return true;

                if (sel[0] == -1) { sel[0]=x; sel[1]=y; return true; }

                if (sonAdyacentesOrto(sel[0], sel[1], x, y)) {
                    pick.run(sel[0], sel[1], x, y);
                    restoreInput();
                } else {
                    sel[0]=x; sel[1]=y;
                }
                return true;
            }
        };

        if (cartasHUD != null)
            Gdx.input.setInputProcessor(new InputMultiplexer(ad, cartasHUD.getStage(), input));
        else
            Gdx.input.setInputProcessor(new InputMultiplexer(ad, input));
    }

    private void pushSelector(BiConsumer<Integer,Integer> onPick){
        final InputAdapter ad = new InputAdapter(){
            @Override public boolean touchDown(int sx,int sy,int pointer,int button){
                Vector2 w = new Vector2(sx,sy);
                viewport.unproject(w);
                int x = (int)((w.x - tablero.getOrigenX())/tablero.getTamCelda());
                int y = (int)((w.y - tablero.getOrigenY())/tablero.getTamCelda());
                if (tablero.enTablero(x,y)) onPick.accept(x,y);
                restoreInput();
                return true;
            }
        };
        if (cartasHUD != null) Gdx.input.setInputProcessor(new InputMultiplexer(ad, cartasHUD.getStage(), input));
        else Gdx.input.setInputProcessor(new InputMultiplexer(ad, input));
    }

    private void restoreInput(){
        if (promoPantalla != null) {
            if (cartasHUD != null)
                Gdx.input.setInputProcessor(new InputMultiplexer(promoPantalla.getStage(), cartasHUD.getStage(), input));
            else
                Gdx.input.setInputProcessor(new InputMultiplexer(promoPantalla.getStage(), input));
        } else {
            if (cartasHUD != null)
                Gdx.input.setInputProcessor(new InputMultiplexer(cartasHUD.getStage(), input));
            else
                Gdx.input.setInputProcessor(new InputMultiplexer(input));
        }
    }
}
