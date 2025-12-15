package Principal.juego.red;

public class LanzadorServidorAjedrez {
    public static ServidorAjedrez servidor;

    public static void main(String[] args) {
        servidor = new ServidorAjedrez();
        servidor.start();
    }
}
