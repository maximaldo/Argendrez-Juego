package Principal.juego.interfaz;


import Principal.juego.Principal;
import Principal.juego.elementos.*;
import Principal.juego.elementos.EfectosVisuales;
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
import Principal.juego.red.ClienteAjedrez;

import java.util.function.BiConsumer;

import Principal.juego.variantes.cartas.ManoCartas;
import Principal.juego.variantes.cartas.TipoCarta;

public class JuegoPantalla implements Screen {

    private final Principal app;

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


    // --- Red ---
    private ClienteAjedrez clienteRed;


    private CartasHUD cartasHUD;
    private final ManoCartas manoBlancas = new ManoCartas();
    private final ManoCartas manoNegras  = new ManoCartas();
    private boolean cartaJugadaEsteTurno = false;

    private EfectosVisuales efectos;

    private PromocionPantalla promoPantalla;

    private BitmapFont fontMsg;
    private final GlyphLayout layout = new GlyphLayout();


    private float endTimer = 0f;  // tiempo transcurrido luego de terminar la partida

    public JuegoPantalla(Principal app, float segPorTurno) {
        this(app, segPorTurno, false, false);
    }

    public JuegoPantalla(Principal app, float segPorTurno, boolean modoBonus, boolean modoExtra) {
        this.app = app;
        this.segPorTurno = segPorTurno;
        this.modoBonus = modoBonus;
        this.modoExtra = modoExtra;
    }


    @Override public void show() {
        EfectosVisuales.iniciar();
        batch = new SpriteBatch();
        efectos = new EfectosVisuales();
        viewport = new FitViewport(TAM_VIRTUAL, TAM_VIRTUAL);
        viewport.apply(true);

        tablero = new GestorPiezas(modoExtra);
        // reservar espacio para panel derecho si está el modo extra
        tablero.onResize((int) (viewport.getWorldWidth() - (modoExtra ? PANEL_W : 0)),
            (int) viewport.getWorldHeight());

        input = new InputJugador(tablero, viewport);
        InputMultiplexer mux = new InputMultiplexer(input);

        // Intentar iniciar modo online usando UDP + broadcast (mismos principios que el chat)
        try {
            clienteRed = new ClienteAjedrez(new ClienteAjedrez.ReceptorMensajes() {

                @Override
                public void onColorAsignado(ColorPieza color) {
                    Gdx.app.log("RED", "Color asignado por el servidor: " + color);
                    input.configurarModoOnline(color);
                }

                @Override
                public void onMovimientoRecibido(int sx, int sy, int dx, int dy) {
                    Gdx.app.postRunnable(() -> {
                        if (tablero.moverConAnim(sx, sy, dx, dy)) {
                            EfectosVisuales.limpiar();
                            input.forzarCambioTurno();
                        }
                    });
                }

                @Override
                public void onCartaRecibida(String data) {
                    Gdx.app.postRunnable(() -> aplicarCartaRemota(data));
                }

                @Override
                public void onCartaRobada(ColorPieza color, TipoCarta carta) {
                    Gdx.app.postRunnable(() -> {
                        ManoCartas mano = (color == ColorPieza.BLANCO)
                            ? manoBlancas
                            : manoNegras;

                        if (mano.puedeRobar()) {
                            mano.robar(carta);
                        }
                    });
                }


                public void onConexionEstablecida() {
                    Gdx.app.postRunnable(() -> {
                        Gdx.app.log("RED", "Conexion establecida con el servidor de ajedrez");
                        input.setPuedeJugar(true);
                    });
                }
                @Override
                public void onPromocion(ColorPieza color, TipoPieza tipo) {
                    Gdx.app.postRunnable(() -> {
                        tablero.promocionar(tipo);
                    });
                }

                @Override
                public void onRuletaActualizada(ColorPieza color, int restante) {
                    Gdx.app.postRunnable(() -> {
                        // solo mostramos el contador del turno actual
                        if (color == input.getTurno() && cartasHUD != null) {
                            cartasHUD.setRuletaRestante(restante);
                        }
                    });
                }
                @Override
                public void onServidorCaido() {
                    Gdx.app.postRunnable(() -> {
                        System.out.println("[JUEGO] Servidor caído, volviendo al menú");

                        // cerrar cliente por las dudas
                        if (clienteRed != null) {
                            clienteRed.cerrar();
                        }

                        // volver al menú
                        app.setScreen(new MenuPantalla(app));
                    });
                }
            });


            clienteRed.start();
            input.setCliente(clienteRed);
        } catch (Exception e) {
            clienteRed = null;
            Gdx.app.log("RED", "No se encontro servidor, se juega en modo local");
        }

        Gdx.input.setInputProcessor(mux);

        hud = new Hud(segPorTurno, modoBonus);

        if (modoExtra) {
            cartasHUD = new CartasHUD(this::intentarJugarCarta, PANEL_W);

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
        EfectosVisuales.render(  batch, tablero.getTamCelda(),tablero.getOrigenX(), tablero.getOrigenY() );


        batch.end();

        input.dibujarOverlay(batch);

        if (!input.isModoOnline() || input.puedeJugar()) {
            hud.update(delta, input);
            if (modoBonus && hud.hayPerdidaPorTiempo()) {
                ColorPieza perdio = hud.getPerdioPorTiempo();
                tablero.finalizarPorTiempo(
                    perdio == ColorPieza.BLANCO
                        ? ColorPieza.NEGRO
                        : ColorPieza.BLANCO
                );
            }
        }
        hud.draw();

        if (modoExtra && cartasHUD != null) {
            cartasHUD.setTurno(input.getTurno());
            cartasHUD.setMano(manoActual().getCartas());

            boolean esMiTurno = !input.isModoOnline()
                || input.getTurno() == input.getColorLocal();

            cartasHUD.setHabilitado(esMiTurno);

            cartasHUD.act(delta);
            cartasHUD.draw();
        }


        if (tablero.hayPromocionPendiente()
            && promoPantalla == null
            && (
            !input.isModoOnline()
                || tablero.getPromColor() == input.getColorLocal()
        )
        ) {

            promoPantalla = new PromocionPantalla(tablero.getPromColor(), tipo -> {

                tablero.promocionar(tipo);

                // si estás en red, avisar al rival
                if (input.isModoOnline() && clienteRed != null) {
                    clienteRed.enviarPromocion(tablero.getPromColor(), tipo);
                }

                restoreInput();

                promoPantalla.dispose();
                promoPantalla = null;
            });

            if (cartasHUD != null)
                Gdx.input.setInputProcessor(
                    new InputMultiplexer(promoPantalla.getStage(), cartasHUD.getStage(), input)
                );
            else
                Gdx.input.setInputProcessor(
                    new InputMultiplexer(promoPantalla.getStage(), input)
                );
        }
        if (promoPantalla != null) { promoPantalla.act(delta); promoPantalla.draw(); }

        if (tablero.hayJuegoTerminado()) {

            // Mostrar ganador
            String msg = "GANAN " +
                (tablero.getGanador() == ColorPieza.BLANCO ? "BLANCAS" : "NEGRAS");

            layout.setText(fontMsg, msg);

            float boardW = tablero.getTamCelda() * 8f;
            float cx = tablero.getOrigenX() + (boardW - layout.width) / 2f;
            float cy = tablero.getOrigenY() + (boardW + layout.height) / 2f;

            batch.begin();
            fontMsg.draw(batch, layout, cx, cy);
            batch.end();


            // Contador de 3 segundos
            endTimer += delta;

            if (endTimer >= 3f) {
                app.setScreen(new MenuPantalla(app));
                dispose();
                return;
            }


            return;
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
    @Override
    public void dispose() {
        if (clienteRed != null) {
            clienteRed.cerrar();
            clienteRed = null;
        }

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
        if (turnoPrevio == null) {
            turnoPrevio = turnoActual;
            return;
        }

        if (turnoActual != turnoPrevio) {
            tablero.tickEfectosAlComenzarTurno(turnoActual);
            cartaJugadaEsteTurno = false;
            turnoPrevio = turnoActual;
        }
    }

    private void aplicarCartaRemota(String data) {
        String[] p = data.split(",");

        TipoCarta carta = TipoCarta.valueOf(p[0]);
        ColorPieza quienLaUso = ColorPieza.valueOf(p[1]);

        // GASTAR LA CARTA EN LA MANO DEL QUE LA USÓ
        if (!input.isModoOnline() || quienLaUso != input.getColorLocal()) {
            ManoCartas manoDelQueUso =
                (quienLaUso == ColorPieza.BLANCO)
                    ? manoBlancas
                    : manoNegras;

            manoDelQueUso.gastar(carta);
        }

        switch (carta) {

            case SPRINT: {
                int x = Integer.parseInt(p[2]);
                int y = Integer.parseInt(p[3]);

                tablero.aplicarSprint(x, y);
                EfectosVisuales.dispararEfecto(x, y, TipoCarta.SPRINT);
                break;
            }


            case FORTIFICACION: {
                int x = Integer.parseInt(p[2]);
                int y = Integer.parseInt(p[3]);

                tablero.aplicarFortificacion(x, y, quienLaUso);
                EfectosVisuales.dispararEfecto(x, y, TipoCarta.FORTIFICACION);
                break;
            }

            case CONGELAR: {
                int x = Integer.parseInt(p[2]);
                int y = Integer.parseInt(p[3]);

                tablero.aplicarCongelar(x, y, quienLaUso);
                EfectosVisuales.dispararEfecto(x, y, TipoCarta.CONGELAR);
                break;
            }

            case REAGRUPACION: {
                int x1 = Integer.parseInt(p[2]);
                int y1 = Integer.parseInt(p[3]);
                int x2 = Integer.parseInt(p[4]);
                int y2 = Integer.parseInt(p[5]);

                tablero.intercambiar(x1, y1, x2, y2);
                EfectosVisuales.dispararEfecto(x1, y1, TipoCarta.REAGRUPACION);
                EfectosVisuales.dispararEfecto(x2, y2, TipoCarta.REAGRUPACION);
                break;
            }


            case TELEPEON: {
                int x = Integer.parseInt(p[2]);
                int y = Integer.parseInt(p[3]);

                Pieza pz = tablero.obtener(x, y);
                if (pz != null && pz.tipo == TipoPieza.PEON) {
                    int dir = pz.color.dirPeon();
                    int nx = x;
                    int ny = y + dir;

                    if (tablero.enTablero(nx, ny) && tablero.obtener(nx, ny) == null) {
                        tablero.mover(x, y, nx, ny);
                        EfectosVisuales.dispararEfecto(x, y, TipoCarta.TELEPEON, pz.color);
                        EfectosVisuales.dispararEfecto(nx, ny, TipoCarta.TELEPEON, pz.color);
                    }
                }
                break;
            }


            case PRORROGA: {
                hud.sumarBonus(quienLaUso, 40, segPorTurno * 2f);
                break;
            }

        }
    }



    private void intentarJugarCarta(TipoCarta carta) {
        if (input.isModoOnline() && input.getTurno() != input.getColorLocal()) {
            return;
        }
        if (clienteRed != null && input.getTurno() != input.getColorLocal()) {
            return;
        }

        if (!modoExtra || tablero.hayJuegoTerminado() || cartaJugadaEsteTurno)
            return;

        switch (carta) {

            case PRORROGA:
                hud.sumarBonus(input.getTurno(), 40, segPorTurno * 2f);
                manoActual().gastar(carta);
                cartaJugadaEsteTurno = true;

                if (clienteRed != null) {
                    clienteRed.enviarCarta(
                        "PRORROGA," + input.getTurno()
                    );
                }
                break;


            case SPRINT:
                seleccionarPropia((x,y)->{
                    tablero.aplicarSprint(x,y);
                    EfectosVisuales.dispararEfecto(x, y, TipoCarta.SPRINT);
                    manoActual().gastar(carta);
                    cartaJugadaEsteTurno = true;

                    if (clienteRed != null) {
                        clienteRed.enviarCarta(
                            "SPRINT," + input.getTurno() + "," + x + "," + y
                        );
                    }
                });
                break;

            case FORTIFICACION:
                seleccionarPropia((x,y)->{
                    tablero.aplicarFortificacion(x,y, input.getTurno());
                    EfectosVisuales.dispararEfecto(x, y, TipoCarta.FORTIFICACION);
                    manoActual().gastar(carta);
                    cartaJugadaEsteTurno = true;

                    if (clienteRed != null) {
                        clienteRed.enviarCarta(
                            "FORTIFICACION," + input.getTurno() + "," + x + "," + y
                        );
                    }
                });
                break;

            case CONGELAR:
                seleccionarRival((x,y)->{
                    tablero.aplicarCongelar(x,y, input.getTurno());
                    EfectosVisuales.dispararEfecto(x, y, TipoCarta.CONGELAR);
                    manoActual().gastar(carta);
                    cartaJugadaEsteTurno = true;

                    if (clienteRed != null) {
                        clienteRed.enviarCarta(
                            "CONGELAR," + input.getTurno() + "," + x + "," + y
                        );
                    }
                });
                break;


            case REAGRUPACION:
                seleccionarDosPropiasAdyacentes((x1,y1,x2,y2)->{
                    tablero.intercambiar(x1,y1,x2,y2);
                    EfectosVisuales.dispararEfecto(x1,y1,TipoCarta.REAGRUPACION);
                    EfectosVisuales.dispararEfecto(x2,y2,TipoCarta.REAGRUPACION);
                    manoActual().gastar(carta);
                    cartaJugadaEsteTurno = true;

                    if (clienteRed != null) {
                        clienteRed.enviarCarta(
                            "REAGRUPACION," + input.getTurno()
                                + "," + x1 + "," + y1
                                + "," + x2 + "," + y2
                        );

                    }
                });
                break;


            case TELEPEON:
                seleccionarPropia((x,y)->{
                    Pieza p = tablero.obtener(x,y);
                    if (p != null && p.tipo == TipoPieza.PEON) {
                        int dir = p.color.dirPeon();
                        int nx = x;
                        int ny = y + dir;
                        if (tablero.enTablero(nx,ny) && tablero.obtener(nx,ny)==null) {
                            tablero.mover(x,y,nx,ny);
                            EfectosVisuales.dispararEfecto(x, y, TipoCarta.TELEPEON, p.color);
                            EfectosVisuales.dispararEfecto(nx, ny, TipoCarta.TELEPEON, p.color);
                        }
                    }

                    manoActual().gastar(carta);
                    cartaJugadaEsteTurno = true;

                    if (clienteRed != null) {
                        clienteRed.enviarCarta(
                            "TELEPEON," + input.getTurno() + "," + x + "," + y
                        );
                    }
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
