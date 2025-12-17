package Principal.juego;

import com.badlogic.gdx.Game;
import Principal.juego.interfaz.MenuPantalla;

public class Principal extends Game {



    @Override
    public void create() {
        setScreen(new MenuPantalla(this));
    }

    public void dispose() {
        super.dispose();
    }
}
