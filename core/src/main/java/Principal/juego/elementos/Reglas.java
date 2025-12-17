package Principal.juego.elementos;

import java.util.ArrayList;
import java.util.List;

import static Principal.juego.elementos.ColorPieza.*;
import static Principal.juego.elementos.TipoPieza.*;

public class Reglas {

    public static List<int[]> movimientosLegales(GestorPiezas t, int x, int y, Pieza pieza, boolean modoExtra) {
        List<int[]> jugadas = new ArrayList<>();
        if (pieza == null) return jugadas;

        if (pieza.tipo == PEON) {
            moverPeon(t, x, y, pieza, jugadas, modoExtra);
        } else if (pieza.tipo.esDeslizador()) {
            int[][] dirs = pieza.tipo.dirs();
            for (int[] d : dirs) {
                int cx = x + d[0], cy = y + d[1];
                while (t.enTablero(cx,cy)) {
                    Pieza otra = t.obtener(cx,cy);
                    if (otra == null) jugadas.add(new int[]{cx,cy});
                    else { if (otra.color != pieza.color) jugadas.add(new int[]{cx,cy}); break; }
                    cx += d[0]; cy += d[1];
                }
            }
        } else { // rey/caballo
            int[][] dirs = pieza.tipo.dirs();
            for (int[] d : dirs) {
                int cx = x + d[0], cy = y + d[1];
                if (!t.enTablero(cx, cy)) continue;
                Pieza otra = t.obtener(cx, cy);
                if (otra == null || otra.color != pieza.color) jugadas.add(new int[]{cx, cy});
            }
        }

        // Carta Sprint tiene +1 ortogonal
        if (t.tieneSprint(x, y)) {
            int[][] ort = {{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d : ort) {
                int cx = x + d[0], cy = y + d[1];
                if (t.enTablero(cx, cy) && t.obtener(cx, cy) == null) jugadas.add(new int[]{cx, cy});
            }
        }
        return jugadas;
    }

    private static void moverPeon(GestorPiezas t, int x, int y, Pieza yo, List<int[]> jugadas, boolean modoExtra) {
        int dir = yo.color.dirPeon(); // +1 blancas / -1 negras
        int ny  = y + dir;

        if (t.enTablero(x, ny) && t.obtener(x, ny) == null) jugadas.add(new int[]{x, ny});

        int inicio = yo.color.filaInicialPeon();
        int ny2 = y + 2*dir;
        if (y == inicio && t.obtener(x, ny) == null && t.enTablero(x, ny2) && t.obtener(x, ny2) == null)
            jugadas.add(new int[]{x, ny2});

        int[] dxs = {-1, +1};
        for (int dx : dxs) {
            int cx = x + dx, cy = y + dir;
            if (t.enTablero(cx, cy)) {
                Pieza otra = t.obtener(cx, cy);
                if (otra != null && otra.color != yo.color) jugadas.add(new int[]{cx, cy});
            }
        }

        // Peón evolucionado diagonal hacia atrás (solo movimiento)
        if (modoExtra) {
            boolean cruzoMitad = (yo.color == BLANCO) ? (y >= 4) : (y <= 3);
            if (cruzoMitad) {
                int backY = y - dir;
                for (int dx : dxs) {
                    int cx = x + dx, cy = backY;
                    if (t.enTablero(cx, cy) && t.obtener(cx, cy) == null) jugadas.add(new int[]{cx, cy});
                }
            }
        }

        // Sprint también aplica al peón
        if (t.tieneSprint(x, y)) {
            int[][] ort = {{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d : ort) {
                int cx = x + d[0], cy = y + d[1];
                if (t.enTablero(cx, cy) && t.obtener(cx, cy) == null) jugadas.add(new int[]{cx, cy});
            }
        }
    }
}
