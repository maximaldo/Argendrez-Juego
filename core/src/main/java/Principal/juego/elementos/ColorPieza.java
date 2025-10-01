package Principal.juego.elementos;

public enum ColorPieza {
    BLANCO(+1, 1, 0, "w_"),
    NEGRO(-1, 6, 7, "b_");

    private final int dirPeon;           // +1 o -1 según el color
    private final int filaInicialPeon;   // 1 blancas, 6 negras
    private final int filaMayor;         // fila “de fondo”: 0 blancas, 7 negras
    private final String prefijoArchivo; // prefijo para sprites: w_ / b_

    ColorPieza(int dirPeon, int filaInicialPeon, int filaMayor, String prefijoArchivo) {
        this.dirPeon = dirPeon;
        this.filaInicialPeon = filaInicialPeon;
        this.filaMayor = filaMayor;
        this.prefijoArchivo = prefijoArchivo;
    }

    public int dirPeon() { return dirPeon; }
    public int filaInicialPeon() { return filaInicialPeon; }
    public int filaMayor() { return filaMayor; }
    public String prefijoArchivo() { return prefijoArchivo; }

    public ColorPieza opuesto() { return this == BLANCO ? NEGRO : BLANCO; }
}
