package Principal.juego.elementos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import Principal.juego.variantes.cartas.TipoCarta;



public class EfectosVisuales {

    private static Texture texSprint;
    private static Texture texReagrupar;
    private static Texture texCandado;
    private static Texture texHielo;

    private static float duracion = 4.0f;  // cuánto dura el efecto visual
    private static float tiempo = 0f;

    private static int casX = -1, casY = -1;  // casilla afectada
    private static Texture texturaActual = null;

    public static void iniciar() {
        texSprint    = new Texture(Gdx.files.internal("Flecha_arriba.png"));
        texReagrupar = new Texture(Gdx.files.internal("Flechas_opuestas.png"));
        texCandado   = new Texture(Gdx.files.internal("Candado.png"));
        texHielo     = new Texture(Gdx.files.internal("coponieve.png"));
    }

    /** Llamado por GestorPiezas después de aplicar una carta */
    public static void dispararEfecto(int x, int y, TipoCarta carta) {

        casX = x;
        casY = y;
        tiempo = -1;

        switch (carta) {
            case SPRINT:
                texturaActual = texSprint;
                break;

            case REAGRUPACION:
                texturaActual = texReagrupar;
                break;

            case FORTIFICACION:
                texturaActual = texCandado;
                break;

            case CONGELAR:
                texturaActual = texHielo;
                break;

            default:
                texturaActual = null;
        }
    }
    public static void limpiar() {
        texturaActual = null;
        tiempo = 0;
    }

    /** Llamado cada frame desde render() */
    public static void render(SpriteBatch batch, float tamCasilla, float origenX, float origenY) {

        if (texturaActual == null) return;

        if (tiempo != -1) {
            if (tiempo <= 0) {
                texturaActual = null;
                return;
            }

            tiempo -= Gdx.graphics.getDeltaTime();
        }


        float alpha = (tiempo == -1) ? 1f : tiempo / duracion;

        batch.setColor(1, 1, 1, alpha);

        float px = origenX + casX * tamCasilla;
        float py = origenY + casY * tamCasilla;


        batch.draw(
            texturaActual,
            px, py,
            tamCasilla, tamCasilla
        );

        batch.setColor(1, 1, 1, 1); // reset color


    }
}
