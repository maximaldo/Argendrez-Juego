package Principal.juego.interfaz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

import Principal.juego.elementos.ColorPieza;
import Principal.juego.elementos.GestorPiezas;
import Principal.juego.elementos.Pieza;
import Principal.juego.elementos.Reglas;

import static Principal.juego.elementos.ColorPieza.*;

public class InputJugador extends InputAdapter {

    private final GestorPiezas tablero;
    private final Viewport viewport;

    private ColorPieza turno = BLANCO;
    private int selX = -1, selY = -1;
    private final List<int[]> legales = new ArrayList<>();
    private final ShapeRenderer formas = new ShapeRenderer();

    public InputJugador(GestorPiezas tablero, Viewport viewport) {
        this.tablero = tablero;
        this.viewport = viewport;
    }

    public ColorPieza getTurno() { return turno; }
    public void forzarCambioTurno() { turno = (turno == BLANCO) ? NEGRO : BLANCO; limpiarSeleccion(); }

    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        if (tablero.estaAnimando() || tablero.hayPromocionPendiente() || tablero.hayJuegoTerminado()) return true;

        Vector2 world = new Vector2(screenX, screenY);
        viewport.unproject(world);

        int x = (int) ((world.x - tablero.getOrigenX()) / tablero.getTamCelda());
        int y = (int) ((world.y - tablero.getOrigenY()) / tablero.getTamCelda());
        if (!tablero.enTablero(x, y)) { limpiarSeleccion(); return true; }

        if (selX == -1) {
            Pieza p = tablero.obtener(x, y);
            if (p != null && p.color == turno) {
                selX = x; selY = y;
                legales.clear();
                legales.addAll(Reglas.movimientosLegales(tablero, x, y, p, tablero.isModoExtra())); // << flag
            }
        } else {
            for (int[] mv : legales) {
                if (mv[0] == x && mv[1] == y) {
                    if (tablero.moverConAnim(selX, selY, x, y)) {
                        turno = (turno == BLANCO) ? NEGRO : BLANCO;
                        limpiarSeleccion();
                    }
                    return true;
                }
            }
            Pieza otra = tablero.obtener(x, y);
            if (otra != null && otra.color == turno) {
                selX = x; selY = y;
                legales.clear();
                legales.addAll(Reglas.movimientosLegales(tablero, x, y, otra, tablero.isModoExtra()));
            } else {
                limpiarSeleccion();
            }
        }
        return true;
    }

    public void dibujarOverlay(SpriteBatch batch) {
        formas.setProjectionMatrix(batch.getProjectionMatrix());

        // Selección
        formas.begin(ShapeType.Line);
        if (selX != -1) {
            float x = tablero.getOrigenX() + selX * tablero.getTamCelda();
            float y = tablero.getOrigenY() + selY * tablero.getTamCelda();
            formas.setColor(1f, 1f, 0f, 1f);
            formas.rect(x + 2, y + 2, tablero.getTamCelda() - 4, tablero.getTamCelda() - 4);
        }
        formas.end();

        // Destinos
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        formas.begin(ShapeType.Filled);
        formas.setColor(0f, 1f, 1f, 0.45f);
        for (int[] mv : legales) {
            float cx = tablero.getOrigenX() + mv[0] * tablero.getTamCelda() + tablero.getTamCelda() / 2f;
            float cy = tablero.getOrigenY() + mv[1] * tablero.getTamCelda() + tablero.getTamCelda() / 2f;
            formas.circle(cx, cy, tablero.getTamCelda() / 6f);
        }
        formas.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void limpiarSeleccion() { selX = selY = -1; legales.clear(); }
    public void dispose() { formas.dispose(); }
}
