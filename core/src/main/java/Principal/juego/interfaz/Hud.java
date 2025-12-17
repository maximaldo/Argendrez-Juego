package Principal.juego.interfaz;

import Principal.juego.elementos.ColorPieza;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class Hud implements Disposable {

    private final boolean modoBonus;   // truedos relojes (modo EXTRA). false reloj por turno clásico.
    private final float baseSegundos;

    private static final float ALTURA_BARRA = 40f;

    private final Stage stage;
    private final Label lblTurno, lblTiempoIzq, lblTiempoDer; // izq = BLANCAS, der = NEGRAS
    private final Texture tex1x1;

    // Modo EXTRA
    private float tiempoBlancas, tiempoNegras;
    private ColorPieza perdioPorTiempo = null; // si queda sin tiempo, se marca el color que perdió

    // Modo clásico (por turno)
    private float tiempoTurno;
    private ColorPieza ultimoTurno = null;

    public Hud(float segundos, boolean modoBonus) {
        this.modoBonus   = modoBonus;
        this.baseSegundos = segundos;
        this.tiempoBlancas = segundos;
        this.tiempoNegras  = segundos;
        this.tiempoTurno   = segundos;

        stage = new Stage(new ScreenViewport());

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE); pm.fill();
        tex1x1 = new Texture(pm); pm.dispose();

        BitmapFont font = new BitmapFont();
        font.getData().setScale(1.1f);
        LabelStyle style = new LabelStyle(font, Color.WHITE);

        lblTurno = new Label("Turno: -", style);
        lblTiempoIzq = new Label(formatear(segundos), style);
        lblTiempoDer = new Label(formatear(segundos), style);

        Image bg = new Image(tex1x1); bg.setColor(0,0,0,0.45f);

        Table labels = new Table();
        labels.padLeft(10).padRight(10);
        labels.add(lblTurno).left().expandX();
        labels.add(lblTiempoIzq).right().padRight(10);
        labels.add(new Label("|", style)).padRight(10);
        labels.add(lblTiempoDer).right();

        Stack stack = new Stack();
        stack.add(bg); stack.add(labels);

        Table top = new Table(); top.setFillParent(true); top.top();
        top.add(stack).growX().height(ALTURA_BARRA);

        stage.addActor(top);
    }

    public void update(float dt, InputJugador input) {
        ColorPieza turno = input.getTurno();
        lblTurno.setText("Turno: " + (turno == ColorPieza.BLANCO ? "BLANCAS" : "NEGRAS"));

        if (modoBonus) {
            if (perdioPorTiempo == null) {
                if (turno == ColorPieza.BLANCO) {
                    tiempoBlancas -= dt;
                    if (tiempoBlancas <= 0f) { tiempoBlancas = 0f; perdioPorTiempo = ColorPieza.BLANCO; }
                } else {
                    tiempoNegras -= dt;
                    if (tiempoNegras <= 0f) { tiempoNegras = 0f; perdioPorTiempo = ColorPieza.NEGRO; }
                }
            }
            lblTiempoIzq.setText(formatear(tiempoBlancas));
            lblTiempoDer.setText(formatear(tiempoNegras));
        } else {
            // Modo clásico: reloj por turno que se resetea
            if (ultimoTurno == null || turno != ultimoTurno) {
                ultimoTurno = turno; tiempoTurno = baseSegundos;
            }
            tiempoTurno -= dt;
            if (tiempoTurno <= 0f) { tiempoTurno = baseSegundos; input.forzarCambioTurno(); }

            if (turno == ColorPieza.BLANCO) {
                lblTiempoIzq.setText(formatear(tiempoTurno));
                lblTiempoDer.setText(formatear(baseSegundos));
            } else {
                lblTiempoDer.setText(formatear(tiempoTurno));
                lblTiempoIzq.setText(formatear(baseSegundos));
            }
        }

        stage.act(dt);
    }
    //QUIEN PERDIO
    public boolean hayPerdidaPorTiempo() { return modoBonus && perdioPorTiempo != null; }
    public ColorPieza getPerdioPorTiempo() { return perdioPorTiempo; }

    public void sumarBonus(ColorPieza color, int segundos, float limiteMaximo) {
        if (!modoBonus || perdioPorTiempo != null) return;
        if (color == ColorPieza.BLANCO) {
            tiempoBlancas = Math.min(tiempoBlancas + segundos, limiteMaximo);
        } else {
            tiempoNegras = Math.min(tiempoNegras + segundos, limiteMaximo);
        }
    }

    public void draw() { stage.draw(); }
    public void resize(int w, int h) { stage.getViewport().update(w, h, true); }

    private String formatear(float t) {
        int s = Math.max(0, (int)Math.ceil(t));
        int m = s / 60, sec = s % 60;
        return m + ":" + (sec < 10 ? "0" : "") + sec;
    }

    @Override public void dispose() { stage.dispose(); tex1x1.dispose(); }
}
