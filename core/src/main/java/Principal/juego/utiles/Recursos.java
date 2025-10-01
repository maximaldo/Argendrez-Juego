package Principal.juego.utiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.Map;

import Principal.juego.elementos.ColorPieza;
import Principal.juego.elementos.Pieza;
import Principal.juego.elementos.TipoPieza;

public class Recursos {

    public static Texture logo;
    public static Music musicaMenu;

    private static Texture tablero; // "tablero.jpg" en assets/
    private static final Map<String, Texture> piezas = new HashMap<>();

    // --- Menú ---
    public static void cargar() {
        logo = new Texture(Gdx.files.internal("logo.jpg"));          // cambiá si es PNG
        musicaMenu = Gdx.audio.newMusic(Gdx.files.internal("musica_menu.mp3"));
        musicaMenu.setLooping(true);
        musicaMenu.setVolume(0.5f);
    }

    public static void liberar() {
        if (logo != null) { logo.dispose(); logo = null; }
        if (musicaMenu != null) { musicaMenu.dispose(); musicaMenu = null; }
    }

    // --- Tablero/Piezas ---
    public static Texture texturaTablero() {
        if (tablero == null) tablero = new Texture(Gdx.files.internal("tablero.jpg"));
        return tablero;
    }

    public static Texture texturaPieza(Pieza p) {
        String clave = p.color + "_" + p.tipo;
        Texture t = piezas.get(clave);
        if (t != null) return t;

        t = new Texture(Gdx.files.internal(archivoPieza(p)));
        piezas.put(clave, t);
        return t;
    }

    private static String archivoPieza(Pieza p) {
        // ejemplo resultante: w_pawn_1x.png
        return p.color.prefijoArchivo() + p.tipo.archivoBase() + "_1x.png";
    }


    public static void dispose() {
        if (tablero != null) { tablero.dispose(); tablero = null; }
        for (Texture t : piezas.values()) t.dispose();
        piezas.clear();
    }
}
