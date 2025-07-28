package Principal.juego.interfaz;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import Principal.juego.elementos.Pieza;

public class InputJugador implements InputProcessor {

    private Pieza[][] tablero;
    private OrthographicCamera camara;
    private Viewport viewport;

    private int seleccionFila = -1;
    private int seleccionColumna = -1;

    public InputJugador(Pieza[][] tablero, OrthographicCamera camara, Viewport viewport) {
        this.tablero = tablero;
        this.camara = camara;
        this.viewport = viewport;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 coords = new Vector3(screenX, screenY, 0);
        viewport.unproject(coords);

        int tileCol = (int)(coords.x / 64);
        int tileFila = (int)(coords.y / 64);

        if (tileCol < 0 || tileCol > 7 || tileFila < 0 || tileFila > 7) return false;

        if (seleccionFila == -1 && seleccionColumna == -1) {
            if (tablero[tileFila][tileCol] != null) {
                seleccionFila = tileFila;
                seleccionColumna = tileCol;
            }
        } else {
            Pieza piezaSeleccionada = tablero[seleccionFila][seleccionColumna];
            if (piezaSeleccionada != null) {
                tablero[seleccionFila][seleccionColumna] = null;
                tablero[tileFila][tileCol] = piezaSeleccionada;
                piezaSeleccionada.moverA(tileFila, tileCol);
            }
            seleccionFila = -1;
            seleccionColumna = -1;
        }
        return true;
    }

    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }

    @Override
    public boolean touchCancelled(int pointer, int button, int x, int y) {
        return false;
    }
}
