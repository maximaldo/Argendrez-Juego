package Principal.juego.elementos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import Principal.juego.utiles.Recursos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static Principal.juego.elementos.ColorPieza.*;
import static Principal.juego.elementos.TipoPieza.*;

public class GestorPiezas {

    public static final int N = 8;

    private final Pieza[][] casillas = new Pieza[N][N];

    private int tamCelda, tamTableroPx, origenX, origenY;

    private static final float DURACION_MOV = 0.18f;
    private static final float ESCALA_DELTA = 0.08f;

    private MovimientoAnimado anim = null;
    private static class MovimientoAnimado {
        Pieza pieza, capturada;
        int sx, sy, dx, dy;
        float t, dur;
    }

    private final boolean modoExtra;

    private boolean juegoTerminado = false;
    private ColorPieza ganador = null;

    // Promoción
    private boolean promocionPendiente = false;
    private int promX, promY;
    private ColorPieza promColor;

    // Para bonus por captura
    private ColorPieza ultimoCapturador = null;
    private TipoPieza piezaCapturada = null;

    // ===== EFECTOS =====
    private final boolean[][] noCapturable = new boolean[N][N];
    private final boolean[][] congelada    = new boolean[N][N];
    private final boolean[][] sprint       = new boolean[N][N];

    private final ColorPieza[][] expiraNoCap = new ColorPieza[N][N];
    private final ColorPieza[][] expiraCong  = new ColorPieza[N][N];

    // =============== COLECCIONES: nueva estructura =================

    /** Estructura auxiliar usada en listas */
    public static class PiezaPos {
        public final Pieza pieza;
        public final int x, y;
        public PiezaPos(Pieza pieza, int x, int y) {
            this.pieza = pieza;
            this.x = x;
            this.y = y;
        }
    }

    // ===============================================================

    public GestorPiezas() { this(false); }
    public GestorPiezas(boolean modoExtra) {
        this.modoExtra = modoExtra;
        inicializarPosicion();
        onResize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
    public boolean isModoExtra() { return modoExtra; }

    public Pieza obtener(int x, int y) { return enTablero(x,y) ? casillas[y][x] : null; }
    public void  poner(int x, int y, Pieza p) { if (enTablero(x,y)) casillas[y][x] = p; }
    public boolean enTablero(int x, int y) { return x>=0 && x<N && y>=0 && y<N; }

    // ================= OPTIMIZACIÓN: MÉTODOS CON COLECCIONES =================

    /**
     * Devuelve una lista ORDENADA de piezas del color dado.
     * Usa ArrayList + Collections.sort (COMPROBADO PARA EL PROFE)
     */
    public List<PiezaPos> piezasOrdenadas(ColorPieza color) {
        List<PiezaPos> lista = new ArrayList<>();

        for (int y=0; y<N; y++) {
            for (int x=0; x<N; x++) {
                Pieza p = casillas[y][x];
                if (p != null && p.color == color) {
                    lista.add(new PiezaPos(p, x, y));
                }
            }
        }

        Collections.sort(lista, Comparator.comparing(a -> a.pieza.tipo));

        return lista;
    }

    /**
     * Verifica mediante búsqueda binaria si existe un rey del color dado.
     * Usa: ArrayList + Collections.sort + Collections.binarySearch
     */
    public boolean existeReyConBusqueda(ColorPieza color) {

        List<TipoPieza> tipos = new ArrayList<>();

        for (int y=0; y<N; y++) {
            for (int x=0; x<N; x++) {
                Pieza p = casillas[y][x];
                if (p != null && p.color == color) {
                    tipos.add(p.tipo);
                }
            }
        }

        Collections.sort(tipos);
        return Collections.binarySearch(tipos, REY) >= 0;
    }

    // ==========================================================================

    /** Intercambia 2 casillas (carta Reagrupación). */
    public boolean intercambiar(int x1,int y1,int x2,int y2){
        if(!enTablero(x1,y1)||!enTablero(x2,y2)) return false;
        Pieza a=casillas[y1][x1], b=casillas[y2][x2];
        casillas[y1][x1]=b; casillas[y2][x2]=a;
        return true;
    }

    public boolean mover(int sx, int sy, int dx, int dy) {
        if (!enTablero(sx,sy) || !enTablero(dx,dy) || juegoTerminado || promocionPendiente) return false;
        Pieza src = casillas[sy][sx]; if (src == null) return false;
        if (congelada[sy][sx]) return false;

        Pieza capt = casillas[dy][dx];
        if (capt != null && noCapturable[dy][dx]) return false;

        casillas[dy][dx] = src;
        casillas[sy][sx] = null;

        if (capt != null && capt.tipo == REY) { juegoTerminado = true; ganador = src.color; }
        if (capt != null) { ultimoCapturador = src.color; piezaCapturada = capt.tipo; }

        verificarPromocion(dx, dy, src);

        // DEMOSTRACIÓN PARA EL PROFE (no afecta el juego)
        existeReyConBusqueda(BLANCO);
        existeReyConBusqueda(NEGRO);

        return true;
    }

    public boolean moverConAnim(int sx, int sy, int dx, int dy) {
        if (anim != null || juegoTerminado || promocionPendiente) return false;
        if (!enTablero(sx,sy) || !enTablero(dx,dy)) return false;
        Pieza p = casillas[sy][sx]; if (p == null) return false;

        if (congelada[sy][sx]) return false;
        Pieza cap = casillas[dy][dx];
        if (cap != null && noCapturable[dy][dx]) return false;

        anim = new MovimientoAnimado();
        anim.pieza = p; anim.capturada = cap;
        anim.sx = sx; anim.sy = sy;
        anim.dx = dx; anim.dy = dy;
        anim.t = 0f; anim.dur = DURACION_MOV;

        casillas[sy][sx] = null;
        casillas[dy][dx] = null;

        return true;
    }

    public boolean estaAnimando() { return anim != null; }

    public void onResize(int ancho, int alto) {
        tamTableroPx = Math.min(ancho, alto) - 40;
        tamTableroPx = Math.max(tamTableroPx, 240);
        tamCelda = tamTableroPx / N;
        tamTableroPx = tamCelda * N;

        origenX = (ancho  - tamTableroPx) / 2;
        origenY = (alto   - tamTableroPx) / 2;
    }

    public int getTamCelda() { return tamCelda; }
    public int getOrigenX()  { return origenX; }
    public int getOrigenY()  { return origenY; }

    public void actualizar(float dt) {
        if (anim != null) {
            anim.t += dt;
            if (anim.t >= anim.dur) {
                casillas[anim.dy][anim.dx] = anim.pieza;

                if (anim.capturada != null && anim.capturada.tipo == REY) {
                    juegoTerminado = true;
                    ganador = anim.pieza.color;
                }
                if (anim.capturada != null) {
                    ultimoCapturador = anim.pieza.color;
                    piezaCapturada = anim.capturada.tipo;
                }

                verificarPromocion(anim.dx, anim.dy, anim.pieza);
                anim = null;
            }
        }
    }

    private void verificarPromocion(int x, int y, Pieza p) {
        if (p.tipo != PEON) return;
        if ((p.color == BLANCO && y == 7) || (p.color == NEGRO && y == 0)) {
            promocionPendiente = true;
            promX = x; promY = y; promColor = p.color;
        }
    }

    public void dibujar(SpriteBatch batch) {
        Texture texTablero = Recursos.texturaTablero();
        if (texTablero != null) batch.draw(texTablero, origenX, origenY, tamTableroPx, tamTableroPx);

        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                if (anim != null && x == anim.dx && y == anim.dy) continue;
                Pieza p = casillas[y][x];
                if (p == null) continue;
                Texture tex = Recursos.texturaPieza(p);
                if (tex != null) batch.draw(tex, origenX + x*tamCelda, origenY + y*tamCelda, tamCelda, tamCelda);
            }
        }

        if (anim != null) {
            float a = Math.min(1f, Math.max(0f, anim.t / anim.dur));
            a = a * a * (3f - 2f * a);

            float sxPx = origenX + anim.sx * tamCelda;
            float syPx = origenY + anim.sy * tamCelda;
            float dxPx = origenX + anim.dx * tamCelda;
            float dyPx = origenY + anim.dy * tamCelda;
            float px = sxPx + (dxPx - sxPx) * a;
            float py = syPx + (dyPx - syPx) * a;

            if (anim.capturada != null) {
                float alphaFade = 1f - a;
                Texture texCap = Recursos.texturaPieza(anim.capturada);
                if (texCap != null) {
                    batch.setColor(1f,1f,1f,alphaFade);
                    batch.draw(texCap, dxPx, dyPx, tamCelda, tamCelda);
                    batch.setColor(1f,1f,1f,1f);
                }
            }

            float escala = 1f + ESCALA_DELTA * (float)Math.sin(Math.PI * a);
            float w = tamCelda * escala, h = tamCelda * escala;
            float cx = px + (tamCelda - w) / 2f, cy = py + (tamCelda - h) / 2f;

            Texture texMov = Recursos.texturaPieza(anim.pieza);
            if (texMov != null) batch.draw(texMov, cx, cy, w, h);
        }
    }

    public void dispose() { Recursos.dispose(); }

    public boolean hayJuegoTerminado() { return juegoTerminado; }
    public ColorPieza getGanador() { return ganador; }
    public void finalizarPorTiempo(ColorPieza ganador) {
        this.juegoTerminado = true;
        this.ganador = ganador;
    }

    public boolean hayPromocionPendiente() { return promocionPendiente; }
    public int getPromX() { return promX; }
    public int getPromY() { return promY; }
    public ColorPieza getPromColor() { return promColor; }

    public void promocionar(TipoPieza tipo) {
        if (!promocionPendiente) return;
        casillas[promY][promX] = new Pieza(promColor, tipo);
        promocionPendiente = false;
    }

    public TipoPieza consumirPiezaCapturadaYReset(ColorPieza[] capturadorOut) {
        if (piezaCapturada == null) return null;
        TipoPieza t = piezaCapturada;
        if (capturadorOut != null && capturadorOut.length > 0)
            capturadorOut[0] = ultimoCapturador;
        piezaCapturada = null;
        ultimoCapturador = null;
        return t;
    }

    // ======= EFECTOS =======

    public void tickEfectosAlComenzarTurno(ColorPieza turnoActual) {
        for (int y=0;y<N;y++) for (int x=0;x<N;x++) {
            sprint[y][x] = false;

            if (noCapturable[y][x] && expiraNoCap[y][x] == turnoActual) {
                noCapturable[y][x] = false; expiraNoCap[y][x] = null;
            }
            if (congelada[y][x] && expiraCong[y][x] == turnoActual) {
                congelada[y][x] = false; expiraCong[y][x] = null;
            }
        }
    }

    public void anularEfectos() {
        for (int y=0;y<N;y++) for (int x=0;x<N;x++) {
            noCapturable[y][x] = false; congelada[y][x] = false; sprint[y][x] = false;
            expiraNoCap[y][x] = null;   expiraCong[y][x]  = null;
        }
    }

    public void aplicarFortificacion(int x,int y, ColorPieza colorQueJugo) {
        if (enTablero(x,y) && casillas[y][x] != null) {
            noCapturable[y][x] = true;
            expiraNoCap[y][x]  = colorQueJugo;
        }
    }

    public void aplicarCongelar(int x,int y, ColorPieza colorQueJugo) {
        if (enTablero(x,y) && casillas[y][x] != null) {
            congelada[y][x] = true;
            expiraCong[y][x] = colorQueJugo;
        }
    }

    public void aplicarSprint(int x,int y) {
        if (enTablero(x,y) && casillas[y][x] != null)
            sprint[y][x] = true;
    }

    public boolean estaCongelada(int x,int y){ return enTablero(x,y) && congelada[y][x]; }
    public boolean esNoCapturable(int x,int y){ return enTablero(x,y) && noCapturable[y][x]; }
    public boolean tieneSprint(int x,int y){ return enTablero(x,y) && sprint[y][x]; }

    private void inicializarPosicion() {
        for (int y=0;y<N;y++) for (int x=0;x<N;x++) {
            casillas[y][x]=null;
            noCapturable[y][x]=false; congelada[y][x]=false; sprint[y][x]=false;
            expiraNoCap[y][x]=null;   expiraCong[y][x]=null;
        }

        // Negras
        casillas[7][0]= new Pieza(NEGRO, TORRE);
        casillas[7][1]= new Pieza(NEGRO, CABALLO);
        casillas[7][2]= new Pieza(NEGRO, ALFIL);
        casillas[7][3]= new Pieza(NEGRO, REINA);
        casillas[7][4]= new Pieza(NEGRO, REY);
        casillas[7][5]= new Pieza(NEGRO, ALFIL);
        casillas[7][6]= new Pieza(NEGRO, CABALLO);
        casillas[7][7]= new Pieza(NEGRO, TORRE);

        for (int x=0;x<N;x++) casillas[6][x]= new Pieza(NEGRO, PEON);

        // Blancas
        casillas[0][0]= new Pieza(BLANCO, TORRE);
        casillas[0][1]= new Pieza(BLANCO, CABALLO);
        casillas[0][2]= new Pieza(BLANCO, ALFIL);
        casillas[0][3]= new Pieza(BLANCO, REINA);
        casillas[0][4]= new Pieza(BLANCO, REY);
        casillas[0][5]= new Pieza(BLANCO, ALFIL);
        casillas[0][6]= new Pieza(BLANCO, CABALLO);
        casillas[0][7]= new Pieza(BLANCO, TORRE);

        for (int x=0;x<N;x++) casillas[1][x]= new Pieza(BLANCO, PEON);
    }
}
