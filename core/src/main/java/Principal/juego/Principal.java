package Principal.juego;

import Principal.juego.red.LanzadorServidorAjedrez;
import com.badlogic.gdx.Game;
import Principal.juego.interfaz.MenuPantalla;

public class Principal extends Game {
    @Override
    public void create() {
        setScreen(new MenuPantalla(this));
    }
    @Override
    public void dispose() {

        if (LanzadorServidorAjedrez.servidor != null) {
            LanzadorServidorAjedrez.servidor.cerrarServidor();
        }

        super.dispose();
    }
}
