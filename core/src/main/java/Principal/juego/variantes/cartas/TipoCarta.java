package Principal.juego.variantes.cartas;

public enum TipoCarta {
    SPRINT("Sprint","+1 paso recto extra sin capturar este turno"),
    FORTIFICACION("Fortificación","La pieza elegida no puede ser capturada durante el turno del rival"),
    CONGELAR("Congelar","Una pieza rival no puede moverse en su próximo turno"),
    REAGRUPACION("Reagrupación","Intercambia dos piezas propias adyacentes ortogonalmente"),
    TELEPEON("Telepeón","Un peón propio avanza +1 si la casilla está libre (gratis)"),
    PRORROGA("Prórroga","Suma +40s a tu reloj inmediatamente");

    public final String nombre;
    public final String descripcion;

    TipoCarta(String n, String d) { this.nombre = n; this.descripcion = d; }
}
