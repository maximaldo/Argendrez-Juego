package Principal.juego.elementos;

public enum TipoPieza {
    REY   (false, new int[][]{{ 1,0},{-1,0},{0,1},{0,-1},{ 1,1},{ 1,-1},{-1,1},{-1,-1}}, 1,          10_000, 'k', "king"),
    REINA (true,  new int[][]{{ 1,0},{-1,0},{0,1},{0,-1},{ 1,1},{ 1,-1},{-1,1},{-1,-1}}, Integer.MAX_VALUE, 9, 'q', "queen"),
    TORRE (true,  new int[][]{{ 1,0},{-1,0},{0,1},{0,-1}},                                Integer.MAX_VALUE, 5, 'r', "rook"),
    ALFIL (true,  new int[][]{{ 1,1},{ 1,-1},{-1,1},{-1,-1}},                              Integer.MAX_VALUE, 3, 'b', "bishop"),
    CABALLO(false,new int[][]{{ 1,2},{ 2,1},{ 2,-1},{ 1,-2},{-1,-2},{-2,-1},{-2,1},{-1,2}},1,          3, 'n', "knight"),
    PEON  (false, null,                                                                     1,          1, 'p', "pawn"); // especial

    private final boolean deslizador;
    private final int[][] direcciones; // offsets (para CABALLO son saltos; para deslizadores, unitarios)
    private final int maxPasos;        // 1 para rey/caballo; infinito para deslizadores
    private final int valor;           // material
    private final char fen;            // letra FEN en minúscula
    private final String archivoBase;  // base de sprite: "pawn","rook",...

    TipoPieza(boolean deslizador, int[][] direcciones, int maxPasos, int valor, char fen, String archivoBase) {
        this.deslizador = deslizador;
        this.direcciones = direcciones;
        this.maxPasos = maxPasos;
        this.valor = valor;
        this.fen = fen;
        this.archivoBase = archivoBase;
    }

    public boolean esDeslizador() { return deslizador; }
    public int[][] dirs() { return direcciones; }
    public int maxPasos() { return maxPasos; }
    public int valor() { return valor; }
    public char fen() { return fen; }
    public String archivoBase() { return archivoBase; }

    public static boolean esPromocionValida(TipoPieza t) {
        return t == REINA || t == TORRE || t == ALFIL || t == CABALLO;
    }

    public static TipoPieza desdeFen(char c) {
        switch (Character.toLowerCase(c)) {
            case 'k': return REY;
            case 'q': return REINA;
            case 'r': return TORRE;
            case 'b': return ALFIL;
            case 'n': return CABALLO;
            case 'p': return PEON;
            default: throw new IllegalArgumentException("FEN inválido: " + c);
        }
    }
}
