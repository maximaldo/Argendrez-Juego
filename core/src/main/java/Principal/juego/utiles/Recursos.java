package Principal.juego.utiles;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;

public class Recursos {
    public static Texture tablero;
    public static Texture piezas;
    public static Texture logo;
    public static Music musicaMenu;

    public static void cargar() {
        tablero = new Texture(Gdx.files.internal("tablero.jpg"));
        piezas = new Texture(Gdx.files.internal("piezas_spritesheet.png"));
        logo = new Texture(Gdx.files.internal("logo.jpg")); // podés reemplazar el archivo
        musicaMenu = Gdx.audio.newMusic(Gdx.files.internal("musica_menu.mp3"));
        musicaMenu.setLooping(true);
        musicaMenu.setVolume(0.6f); // volumen entre 0 y 1
        musicaMenu.play();
    }

    public static void liberar() {
        tablero.dispose();
        piezas.dispose();
        logo.dispose();
        musicaMenu.dispose();
    }
}
