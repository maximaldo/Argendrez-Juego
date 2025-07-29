package Principal.juego.interfaz;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import Principal.juego.elementos.GestorPiezas;

public class JuegoPantalla extends ScreenAdapter {
    private SpriteBatch batch;
    private Texture casillaClara;
    private Texture casillaOscura;
    private OrthographicCamera camara;
    private GestorPiezas gestorPiezas;

    private static final int FILAS = 8;
    private static final int COLUMNAS = 8;

    public JuegoPantalla() {
        batch = new SpriteBatch();
        casillaClara = new Texture("square brown light_1x.png");
        casillaOscura = new Texture("square brown dark_1x.png");
        camara = new OrthographicCamera();
        camara.setToOrtho(false, 800, 800);
        gestorPiezas = new GestorPiezas();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(1, 1, 1, 1);

        camara.update();
        batch.setProjectionMatrix(camara.combined);

        batch.begin();

        float tamanioCasilla = 80; // 800 / 10 deja margen
        float offsetX = (800 - COLUMNAS * tamanioCasilla) / 2f;
        float offsetY = (800 - FILAS * tamanioCasilla) / 2f;

        for (int fila = 0; fila < FILAS; fila++) {
            for (int col = 0; col < COLUMNAS; col++) {
                Texture casilla = (fila + col) % 2 == 0 ? casillaClara : casillaOscura;
                batch.draw(casilla, offsetX + col * tamanioCasilla, offsetY + fila * tamanioCasilla, tamanioCasilla, tamanioCasilla);
            }
        }

        gestorPiezas.render(batch, tamanioCasilla, offsetX, offsetY);

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        casillaClara.dispose();
        casillaOscura.dispose();
    }
}
