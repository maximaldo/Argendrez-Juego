package Principal.juego.variantes.cartas;

import java.util.ArrayList;
import java.util.List;

public class ManoCartas {
    private static final int LIMITE = 2;
    private final List<TipoCarta> cartas = new ArrayList<>(LIMITE);

    public List<TipoCarta> getCartas() { return cartas; }
    public boolean puedeRobar() { return cartas.size() < LIMITE; }
    public boolean robar(TipoCarta t) { if (!puedeRobar()) return false; cartas.add(t); return true; }
    public boolean contiene(TipoCarta t) { return cartas.contains(t); }
    public boolean gastar(TipoCarta t) { return cartas.remove(t); }
}
