package Principal.juego.interfaz;

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

import Principal.juego.elementos.ColorPieza;

public class Hud implements Disposable {

    private final float durTurno;          // segundos por turno (configurable)
    private static final float ALTURA_BARRA = 40f;

    private final Stage stage;
    private final Label lblTurno;
    private final Label lblTiempo;

    private float tiempoRestante;
    private ColorPieza ultimoTurno = null;

    private final Texture tex1x1;

    public Hud(float segundosPorTurno) {
        this.durTurno = segundosPorTurno;
        this.tiempoRestante = durTurno;

        stage = new Stage(new ScreenViewport());

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        tex1x1 = new Texture(pm);
        pm.dispose();

        BitmapFont font = new BitmapFont();
        font.getData().setScale(1.1f);
        LabelStyle style = new LabelStyle(font, Color.WHITE);

        lblTurno  = new Label("Turno: -", style);
        lblTiempo = new Label(formatear(tiempoRestante), style);

        Image bg = new Image(tex1x1);
        bg.setColor(0f, 0f, 0f, 0.45f);

        Table labels = new Table();
        labels.padLeft(10).padRight(10);
        labels.add(lblTurno).left().expandX();
        labels.add(lblTiempo).right();

        Stack stack = new Stack();
        stack.add(bg);
        stack.add(labels);

        Table top = new Table();
        top.setFillParent(true);
        top.top();
        top.add(stack).growX().height(ALTURA_BARRA);

        stage.addActor(top);
    }

    public void update(float dt, InputJugador input) {
        ColorPieza turnoActual = input.getTurno();
        if (ultimoTurno == null || turnoActual != ultimoTurno) {
            ultimoTurno = turnoActual;
            tiempoRestante = durTurno;
            lblTurno.setText("Turno: " + (turnoActual == ColorPieza.BLANCO ? "BLANCAS" : "NEGRAS"));
        }

        tiempoRestante -= dt;
        if (tiempoRestante <= 0f) {
            input.forzarCambioTurno();
            ultimoTurno = input.getTurno();
            lblTurno.setText("Turno: " + (ultimoTurno == ColorPieza.BLANCO ? "BLANCAS" : "NEGRAS"));
            tiempoRestante = durTurno;
        }

        lblTiempo.setText(formatear(tiempoRestante));
        stage.act(dt);
    }

    public void draw() { stage.draw(); }

    public void resize(int width, int height) { stage.getViewport().update(width, height, true); }

    private String formatear(float t) {
        int s = Math.max(0, (int)Math.ceil(t));
        int m = s / 60;
        int sec = s % 60;
        return m + ":" + (sec < 10 ? "0" : "") + sec;
    }

    @Override
    public void dispose() {
        stage.dispose();
        tex1x1.dispose();
    }
}
