package Principal.juego.elementos;

import java.util.ArrayList;
import java.util.List;

import static Principal.juego.elementos.ColorPieza.*;
import static Principal.juego.elementos.TipoPieza.*;

public class Reglas {

    public static List<int[]> movimientosLegales(GestorPiezas t, int x, int y, Pieza pieza) {
        List<int[]> jugadas = new ArrayList<>();
        if (pieza == null) return jugadas;

        if (pieza.tipo == TipoPieza.PEON) {
            moverPeon(t, x, y, pieza, jugadas);
            return jugadas;
        }

        int[][] dirs = pieza.tipo.dirs();
        if (pieza.tipo.esDeslizador()) {
            // deslizadores: alfil/torre/reina
            for (int[] d : dirs) {
                int cx = x + d[0], cy = y + d[1];
                while (t.enTablero(cx, cy)) {
                    Pieza otra = t.obtener(cx, cy);
                    if (otra == null) jugadas.add(new int[]{cx, cy});
                    else { if (otra.color != pieza.color) jugadas.add(new int[]{cx, cy}); break; }
                    cx += d[0]; cy += d[1];
                }
            }
        } else {
            // rey y caballo: un paso en cada dirección de su lista (salto en el caso del caballo)
            for (int[] d : dirs) {
                int cx = x + d[0], cy = y + d[1];
                if (!t.enTablero(cx, cy)) continue;
                Pieza otra = t.obtener(cx, cy);
                if (otra == null || otra.color != pieza.color) jugadas.add(new int[]{cx, cy});
            }
        }
        return jugadas;
    }


    private static void agregarSiValido(GestorPiezas t, int x, int y, Pieza yo, List<int[]> jugadas) {
        if (!t.enTablero(x,y)) return;
        Pieza otra = t.obtener(x,y);
        if (otra == null || otra.color != yo.color) jugadas.add(new int[]{x,y});
    }

    private static void deslizar(GestorPiezas t, int x, int y, Pieza yo, List<int[]> jugadas, int[][] dirs) {
        for (int[] d : dirs) {
            int cx = x + d[0], cy = y + d[1];
            while (t.enTablero(cx,cy)) {
                Pieza otra = t.obtener(cx,cy);
                if (otra == null) jugadas.add(new int[]{cx,cy});
                else {
                    if (otra.color != yo.color) jugadas.add(new int[]{cx,cy});
                    break;
                }
                cx += d[0]; cy += d[1];
            }
        }
    }

    private static void moverCaballo(GestorPiezas t, int x, int y, Pieza yo, List<int[]> jugadas) {
        int[][] d = {{1,2},{2,1},{2,-1},{1,-2},{-1,-2},{-2,-1},{-2,1},{-1,2}};
        for (int[] v : d) agregarSiValido(t, x+v[0], y+v[1], yo, jugadas);
    }

    private static void moverRey(GestorPiezas t, int x, int y, Pieza yo, List<int[]> jugadas) {
        for (int dx=-1; dx<=1; dx++)
            for (int dy=-1; dy<=1; dy++)
                if (dx!=0 || dy!=0) agregarSiValido(t, x+dx, y+dy, yo, jugadas);
    }

    private static void moverPeon(GestorPiezas t, int x, int y, Pieza yo, List<int[]> jugadas) {
        int dir = yo.color.dirPeon();               // +1 blancas / -1 negras
        int ny  = y + dir;

        if (t.enTablero(x, ny) && t.obtener(x, ny) == null) jugadas.add(new int[]{x, ny});

        int inicio = yo.color.filaInicialPeon();
        int ny2 = y + 2*dir;
        if (y == inicio && t.obtener(x, ny) == null && t.enTablero(x, ny2) && t.obtener(x, ny2) == null)
            jugadas.add(new int[]{x, ny2});

        int[] dxs = {-1, +1};
        for (int dx : dxs) {
            int cx = x + dx, cy = y + dir;
            if (!t.enTablero(cx, cy)) continue;
            Pieza otra = t.obtener(cx, cy);
            if (otra != null && otra.color != yo.color) jugadas.add(new int[]{cx, cy});
        }
    }

}
