package Principal.juego.elementos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import Principal.juego.utiles.Recursos;

import static Principal.juego.elementos.ColorPieza.*;
import static Principal.juego.elementos.TipoPieza.*;

public class GestorPiezas {

    public static final int N = 8;

    private final Pieza[][] casillas = new Pieza[N][N];

    // Layout del tablero en pantalla
    private int tamCelda;
    private int tamTableroPx;
    private int origenX, origenY;

    // -------- Animación de movimiento --------
    private static final float DURACION_MOV = 0.18f; // segundos
    private static final float ESCALA_DELTA = 0.08f; // zoom suave

    private MovimientoAnimado anim = null;

    private static class MovimientoAnimado {
        Pieza pieza;       // pieza que se mueve
        Pieza capturada;   // pieza que estaba en destino (si había)
        int sx, sy;        // celda origen
        int dx, dy;        // celda destino
        float t, dur;      // tiempo transcurrido y duración
    }

    // -------- Estado de fin de juego --------
    private boolean juegoTerminado = false;
    private ColorPieza ganador = null;

    // -------- Promoción --------
    private boolean promocionPendiente = false;
    private int promX, promY;
    private ColorPieza promColor;

    public GestorPiezas() {
        inicializarPosicion();
        onResize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    // ====== Estado del tablero ======
    public Pieza obtener(int x, int y) { return enTablero(x,y) ? casillas[y][x] : null; }
    public void  poner(int x, int y, Pieza p) { if (enTablero(x,y)) casillas[y][x] = p; }
    public boolean enTablero(int x, int y) { return x>=0 && x<N && y>=0 && y<N; }

    /** Movimiento instantáneo (sin animar) */
    public boolean mover(int sx, int sy, int dx, int dy) {
        if (!enTablero(sx,sy) || !enTablero(dx,dy) || juegoTerminado || promocionPendiente) return false;
        Pieza src = casillas[sy][sx];
        if (src == null) return false;

        Pieza capt = casillas[dy][dx];
        casillas[dy][dx] = src;
        casillas[sy][sx] = null;

        // victoria por capturar rey
        if (capt != null && capt.tipo == REY) {
            juegoTerminado = true;
            ganador = src.color;
        }

        // promoción
        verificarPromocion(dx, dy, src);

        return true;
    }

    /** Inicia animación de movimiento; bloquea nuevos movimientos hasta terminar */
    public boolean moverConAnim(int sx, int sy, int dx, int dy) {
        if (anim != null || juegoTerminado || promocionPendiente) return false;
        if (!enTablero(sx,sy) || !enTablero(dx,dy)) return false;

        Pieza p = casillas[sy][sx];
        if (p == null) return false;

        anim = new MovimientoAnimado();
        anim.pieza = p;
        anim.capturada = casillas[dy][dx]; // puede ser null
        anim.sx = sx; anim.sy = sy;
        anim.dx = dx; anim.dy = dy;
        anim.t = 0f;  anim.dur = DURACION_MOV;

        // liberamos origen y destino para controlar qué se dibuja durante la animación
        casillas[sy][sx] = null;
        casillas[dy][dx] = null;
        return true;
    }

    public boolean estaAnimando() { return anim != null; }

    // ====== Layout ======
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

    // ====== Update / Render ======
    public void actualizar(float dt) {
        if (anim != null) {
            anim.t += dt;
            if (anim.t >= anim.dur) {
                // coloca pieza en destino y termina
                casillas[anim.dy][anim.dx] = anim.pieza;

                // victoria por capturar rey
                if (anim.capturada != null && anim.capturada.tipo == REY) {
                    juegoTerminado = true;
                    ganador = anim.pieza.color;
                }

                // promoción
                verificarPromocion(anim.dx, anim.dy, anim.pieza);

                anim = null;
            }
        }
    }

    private void verificarPromocion(int x, int y, Pieza p) {
        if (p.tipo != PEON) return;
        if ((p.color == BLANCO && y == 7) || (p.color == NEGRO && y == 0)) {
            promocionPendiente = true;
            promX = x; promY = y; promColor = p.color; // queda peón hasta que elijan
        }
    }

    public void dibujar(SpriteBatch batch) {
        // 1) Tablero
        Texture texTablero = Recursos.texturaTablero();
        if (texTablero != null) batch.draw(texTablero, origenX, origenY, tamTableroPx, tamTableroPx);

        // 2) Piezas estáticas (ocultar destino mientras anima)
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                if (anim != null && x == anim.dx && y == anim.dy) continue;
                Pieza p = casillas[y][x];
                if (p == null) continue;
                Texture tex = Recursos.texturaPieza(p);
                if (tex != null) batch.draw(tex, origenX + x*tamCelda, origenY + y*tamCelda, tamCelda, tamCelda);
            }
        }

        // 3) Efectos de animación
        if (anim != null) {
            float a = Math.min(1f, Math.max(0f, anim.t / anim.dur));
            a = a * a * (3f - 2f * a); // smoothstep

            float sxPx = origenX + anim.sx * tamCelda;
            float syPx = origenY + anim.sy * tamCelda;
            float dxPx = origenX + anim.dx * tamCelda;
            float dyPx = origenY + anim.dy * tamCelda;
            float px = sxPx + (dxPx - sxPx) * a;
            float py = syPx + (dyPx - syPx) * a;

            // fade de capturada (si hay)
            if (anim.capturada != null) {
                float alphaFade = 1f - a; // 1 -> 0
                Texture texCap = Recursos.texturaPieza(anim.capturada);
                if (texCap != null) {
                    batch.setColor(1f, 1f, 1f, alphaFade);
                    batch.draw(texCap, dxPx, dyPx, tamCelda, tamCelda);
                    batch.setColor(1f, 1f, 1f, 1f);
                }
            }

            // zoom suave de la pieza que se mueve
            float escala = 1f + ESCALA_DELTA * (float)Math.sin(Math.PI * a);
            float w = tamCelda * escala;
            float h = tamCelda * escala;
            float cx = px + (tamCelda - w) / 2f;
            float cy = py + (tamCelda - h) / 2f;

            Texture texMov = Recursos.texturaPieza(anim.pieza);
            if (texMov != null) batch.draw(texMov, cx, cy, w, h);
        }
    }

    public void dispose() { Recursos.dispose(); }

    // ====== API de fin de juego y promoción ======
    public boolean hayJuegoTerminado() { return juegoTerminado; }
    public ColorPieza getGanador() { return ganador; }

    public boolean hayPromocionPendiente() { return promocionPendiente; }
    public int getPromX() { return promX; }
    public int getPromY() { return promY; }
    public ColorPieza getPromColor() { return promColor; }

    public void promocionar(TipoPieza tipo) {
        if (!promocionPendiente) return;
        // reemplazar peón por la pieza elegida
        casillas[promY][promX] = new Pieza(promColor, tipo);
        promocionPendiente = false;
    }

    // ====== Inicial ======
    private void inicializarPosicion() {
        for (int y=0;y<N;y++) for (int x=0;x<N;x++) casillas[y][x]=null;

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
