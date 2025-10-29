package Principal.juego.interfaz;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.Collections;
import java.util.List;

import Principal.juego.elementos.ColorPieza;
import Principal.juego.variantes.cartas.TipoCarta;

/** Panel lateral (derecha) para cartas y ruleta. */
public class CartasHUD {

    public interface Listener { void onJugarCarta(TipoCarta carta); }

    private final Stage stage;
    private final Texture tex1x1;
    private final Label lblTitulo, lblRuleta, lblTurno, lblDesc;
    private final TextButton btnC1, btnC2, btnUsar, btnCancelar;
    private final Listener listener;

    private TipoCarta carta1 = null, carta2 = null;
    private TipoCarta seleccionada = null;

    public CartasHUD(Listener listener, int panelWidth) {
        this.listener = listener;
        stage = new Stage(new ScreenViewport());

        // 1x1 para fondos
        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE); pm.fill();
        tex1x1 = new Texture(pm); pm.dispose();

        // Estilos
        BitmapFont font = new BitmapFont(); font.getData().setScale(1.0f);
        LabelStyle ls = new LabelStyle(font, Color.WHITE);
        TextButtonStyle bs = new TextButtonStyle();
        bs.font = font;
        bs.up = new TextureRegionDrawable(new TextureRegion(tex1x1));

        // Fondo panel
        Image bg = new Image(new TextureRegionDrawable(new TextureRegion(tex1x1)));
        bg.setColor(0f, 0f, 0f, 0.45f);

        // Títulos / etiquetas
        lblTitulo = new Label("Cartas", ls); lblTitulo.setAlignment(Align.center);
        lblRuleta = new Label("Ruleta: 5", ls);
        lblTurno  = new Label("Turno: - (1 carta)", ls);

        // Botones de cartas
        btnC1 = new TextButton("-", bs);
        btnC2 = new TextButton("-", bs);
        estilizarBtnCarta(btnC1);
        estilizarBtnCarta(btnC2);

        // Descripción
        lblDesc = new Label("", ls);
        lblDesc.setWrap(true);
        lblDesc.setAlignment(Align.topLeft);

        // Acciones
        btnUsar = new TextButton("Usar carta", bs);
        btnCancelar = new TextButton("Cancelar", bs);
        btnUsar.getLabel().setColor(Color.WHITE);
        btnCancelar.getLabel().setColor(Color.WHITE);
        btnUsar.setColor(0.12f,0.55f,0.12f,0.95f);    // verde
        btnCancelar.setColor(0.55f,0.12f,0.12f,0.95f); // rojo
        btnUsar.setDisabled(true);

        // Listeners cartas: seleccionar / desseleccionar
        btnC1.addListener(e -> { if (btnC1.isPressed()) toggleSeleccion(carta1, btnC1, btnC2); return false; });
        btnC2.addListener(e -> { if (btnC2.isPressed()) toggleSeleccion(carta2, btnC2, btnC1); return false; });

        // Usar / Cancelar
        btnUsar.addListener(e -> {
            if (btnUsar.isPressed() && seleccionada != null && listener != null) {
                listener.onJugarCarta(seleccionada);
                limpiarSeleccion();
            }
            return false;
        });
        btnCancelar.addListener(e -> { if (btnCancelar.isPressed()) limpiarSeleccion(); return false; });

        // Layout
        Table inner = new Table();
        inner.pad(10);
        inner.defaults().pad(4);

        inner.add(lblTitulo).growX().height(28).row();
        inner.add(lblTurno).left().row();
        inner.add(lblRuleta).left().row();
        inner.add(btnC1).growX().height(38).row();
        inner.add(btnC2).growX().height(38).row();
        inner.add(lblDesc).growX().height(90).row();

        Table acciones = new Table();
        acciones.add(btnUsar).growX().height(32).padRight(4);
        acciones.add(btnCancelar).growX().height(32).padLeft(4);
        inner.add(acciones).growX().row();

        Stack stack = new Stack(); stack.add(bg); stack.add(inner);

        Table root = new Table();
        root.setFillParent(true);
        root.top().right();
        root.add(stack).width(panelWidth).growY().padTop(16).padRight(10);

        stage.addActor(root);
    }

    private void estilizarBtnCarta(TextButton b){
        b.getLabel().setColor(Color.WHITE);
        b.setColor(0.15f,0.18f,0.55f,0.95f);
    }

    private void toggleSeleccion(TipoCarta carta, TextButton self, TextButton other){
        if (carta == null) return;

        if (seleccionada == carta) { // desseleccionar
            limpiarSeleccion();
            return;
        }
        // seleccionar
        seleccionada = carta;
        self.setColor(0.30f,0.35f,0.85f,0.95f); // highlight
        other.setColor(0.15f,0.18f,0.55f,0.95f);
        lblDesc.setText(carta.descripcion);
        btnUsar.setDisabled(false);
    }

    private void limpiarSeleccion(){
        seleccionada = null;
        btnUsar.setDisabled(true);
        lblDesc.setText("");
        btnC1.setColor(0.15f,0.18f,0.55f,0.95f);
        btnC2.setColor(0.15f,0.18f,0.55f,0.95f);
    }

    // -------- API pública --------
    public void setMano(List<TipoCarta> mano) {
        if (mano == null) mano = Collections.emptyList();
        carta1 = mano.size()>0? mano.get(0) : null;
        carta2 = mano.size()>1? mano.get(1) : null;

        btnC1.setText(carta1 != null ? carta1.nombre : "-");
        btnC2.setText(carta2 != null ? carta2.nombre : "-");

        // si la selección quedó inválida, limpiar
        if (seleccionada != null && seleccionada != carta1 && seleccionada != carta2) {
            limpiarSeleccion();
        }
    }

    public void setRuletaRestante(int t) { lblRuleta.setText("Ruleta: " + t); }
    public void setTurno(ColorPieza c) { lblTurno.setText("Turno: " + (c==ColorPieza.BLANCO?"BLANCAS":"NEGRAS") + " (1 carta)"); }

    public Stage getStage() { return stage; }
    public void act(float dt) { stage.act(dt); }
    public void draw() { stage.draw(); }
    public void resize(int w,int h){ stage.getViewport().update(w,h,true); }
    public void dispose(){ stage.dispose(); tex1x1.dispose(); }
}
