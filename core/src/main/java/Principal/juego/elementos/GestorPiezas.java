package Principal.juego.elementos;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayList;

public class GestorPiezas {
    private ArrayList<Pieza> piezas;

    public GestorPiezas() {
        piezas = new ArrayList<>();
        cargarPiezasIniciales();
    }

    private void cargarPiezasIniciales() {
        // Peones blancos
        for (int i = 0; i < 8; i++) {
            piezas.add(new Pieza(new Texture("w_pawn_1x.png"), 1, i, true));
        }

        // Peones negros
        for (int i = 0; i < 8; i++) {
            piezas.add(new Pieza(new Texture("b_pawn_1x.png"), 6, i, false));
        }

        // Piezas blancas
        piezas.add(new Pieza(new Texture("w_rook_1x.png"), 0, 0, true));
        piezas.add(new Pieza(new Texture("w_knight_1x.png"), 0, 1, true));
        piezas.add(new Pieza(new Texture("w_bishop_1x.png"), 0, 2, true));
        piezas.add(new Pieza(new Texture("w_queen_1x.png"), 0, 3, true));
        piezas.add(new Pieza(new Texture("w_king_1x.png"), 0, 4, true));
        piezas.add(new Pieza(new Texture("w_bishop_1x.png"), 0, 5, true));
        piezas.add(new Pieza(new Texture("w_knight_1x.png"), 0, 6, true));
        piezas.add(new Pieza(new Texture("w_rook_1x.png"), 0, 7, true));

        // Piezas negras
        piezas.add(new Pieza(new Texture("b_rook_1x.png"), 7, 0, false));
        piezas.add(new Pieza(new Texture("b_knight_1x.png"), 7, 1, false));
        piezas.add(new Pieza(new Texture("b_bishop_1x.png"), 7, 2, false));
        piezas.add(new Pieza(new Texture("b_queen_1x.png"), 7, 3, false));
        piezas.add(new Pieza(new Texture("b_king_1x.png"), 7, 4, false));
        piezas.add(new Pieza(new Texture("b_bishop_1x.png"), 7, 5, false));
        piezas.add(new Pieza(new Texture("b_knight_1x.png"), 7, 6, false));
        piezas.add(new Pieza(new Texture("b_rook_1x.png"), 7, 7, false));
    }

    public void render(SpriteBatch batch, float tamanioCasilla, float offsetX, float offsetY) {
        for (Pieza pieza : piezas) {
            pieza.render(batch, tamanioCasilla, offsetX, offsetY);
        }
    }
}
