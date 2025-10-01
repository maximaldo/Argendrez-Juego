package Principal.juego.interfaz;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import Principal.juego.elementos.ColorPieza;
import Principal.juego.elementos.Pieza;
import Principal.juego.elementos.TipoPieza;
import Principal.juego.utiles.Recursos;

public class PromocionPantalla implements Disposable {

    public interface Listener { void elegido(TipoPieza tipo); }

    private final Stage stage;
    private final Texture tex1x1;

    public PromocionPantalla(ColorPieza color, Listener listener) {
        stage = new Stage(new ScreenViewport());

        // fondo semitransparente
        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        tex1x1 = new Texture(pm);
        pm.dispose();

        Image fondo = new Image(new TextureRegionDrawable(new TextureRegion(tex1x1)));
        fondo.setColor(0,0,0,0.5f);

        // panel
        Table botones = new Table();
        botones.defaults().pad(8);
        botones.add(boton(color, TipoPieza.REINA,   listener)).size(96);
        botones.add(boton(color, TipoPieza.TORRE,   listener)).size(96);
        botones.add(boton(color, TipoPieza.ALFIL,   listener)).size(96);
        botones.add(boton(color, TipoPieza.CABALLO, listener)).size(96);

        Image panelBg = new Image(new TextureRegionDrawable(new TextureRegion(tex1x1)));
        panelBg.setColor(0f,0f,0f,0.35f);

        Stack panel = new Stack();
        panel.add(panelBg);
        panel.add(botones);

        Table root = new Table();
        root.setFillParent(true);
        root.center();
        root.add(panel).pad(12);

        Stack stack = new Stack();
        stack.setFillParent(true);
        stack.add(fondo);
        stack.add(root);

        stage.addActor(stack);
    }

    private ImageButton boton(ColorPieza color, TipoPieza tipo, Listener listener) {
        Texture tex = Recursos.texturaPieza(new Pieza(color, tipo));
        ImageButton.ImageButtonStyle st = new ImageButton.ImageButtonStyle();
        st.imageUp = new TextureRegionDrawable(new TextureRegion(tex));
        ImageButton b = new ImageButton(st);
        b.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) { listener.elegido(tipo); }
        });
        return b;
    }

    public Stage getStage() { return stage; }

    public void act(float dt) { stage.act(dt); }
    public void draw() { stage.draw(); }

    @Override public void dispose() { stage.dispose(); tex1x1.dispose(); }
}
