package Principal.juego.variantes.cartas;

import java.util.Random;

/** Ruleta con contadores independientes por color. */
public class Ruleta {

    private static final int INTERVALO = 5; // cada 5 turnos PROPIOS roba una carta

    private final Random rng = new Random();

    private int pasosBlancas = INTERVALO;
    private int pasosNegras  = INTERVALO;

    /** Llamar cuando COMIENZA el turno de BLANCAS. */
    public boolean tickParaBlancas() {
        pasosBlancas--;
        if (pasosBlancas <= 0) { pasosBlancas = INTERVALO; return true; }
        return false;
    }

    /** Llamar cuando COMIENZA el turno de NEGRAS. */
    public boolean tickParaNegras() {
        pasosNegras--;
        if (pasosNegras <= 0) { pasosNegras = INTERVALO; return true; }
        return false;
    }
    public void reset() {
        pasosBlancas = INTERVALO;
        pasosNegras  = INTERVALO;
    }

    public int getRestanteBlancas() { return pasosBlancas; }
    public int getRestanteNegras()  { return pasosNegras;  }

    public TipoCarta robarCarta() {
        TipoCarta[] v = TipoCarta.values();
        return v[rng.nextInt(v.length)];
    }
}
