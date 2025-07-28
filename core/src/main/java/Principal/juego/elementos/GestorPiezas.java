package Principal.juego.elementos;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

public class GestorPiezas {
    private Texture spritesheet;
    private List<Pieza> piezas;

    public GestorPiezas() {
        spritesheet = new Texture("sprites/piezas.png");
        piezas = new ArrayList<>();

        // Cargar una pieza blanca y una negra como prueba
        TextureRegion piezaBlanca = new TextureRegion(spritesheet, 0, 0, 64, 64);
        TextureRegion piezaNegra = new TextureRegion(spritesheet, 64, 0, 64, 64);

        piezas.add(new Pieza(piezaBlanca, 1, 0)); // Peón blanco
        piezas.add(new Pieza(piezaNegra, 6, 0)); // Peón negro
    }

    public void render(SpriteBatch batch) {
        for (Pieza pieza : piezas) {
            pieza.render(batch);
        }
    }

    public void dispose() {
        spritesheet.dispose();
    }
}
