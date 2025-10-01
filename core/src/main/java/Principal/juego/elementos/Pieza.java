package Principal.juego.elementos;

public class Pieza {
    public final ColorPieza color;
    public final TipoPieza  tipo;

    public Pieza(ColorPieza color, TipoPieza tipo) {
        this.color = color;
        this.tipo  = tipo;
    }

    @Override public String toString() { return color + "_" + tipo; }
}
