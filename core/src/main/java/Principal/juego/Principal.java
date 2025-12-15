package Principal.juego;

import com.badlogic.gdx.Game;
import Principal.juego.interfaz.MenuPantalla;
import Principal.juego.red.ClienteAjedrez;

public class Principal extends Game {

    private ClienteAjedrez clienteRed;

    @Override
    public void create() {
        setScreen(new MenuPantalla(this));
    }

    public void crearCliente(ClienteAjedrez.ReceptorMensajes receptor) {
        try {
            clienteRed = new ClienteAjedrez(receptor);
            clienteRed.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ClienteAjedrez getClienteRed() {
        return clienteRed;
    }

    public void dispose() {
        super.dispose();
    }
}
