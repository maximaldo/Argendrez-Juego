package Principal.juego.interfaz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import Principal.juego.Principal;
import Principal.juego.utiles.Recursos;

public class MenuPantalla implements Screen {

    private final Principal app;
    private Stage stage;
    private Texture tex1x1;
    private Image logoImg;

    public MenuPantalla(Principal app) { this.app = app; }

    @Override
    public void show() {
        Recursos.cargar();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pm.setColor(1,1,1,1);
        pm.fill();
        tex1x1 = new Texture(pm);
        pm.dispose();

        // logo
        logoImg = new Image(new TextureRegionDrawable(new TextureRegion(Recursos.logo)));

        BitmapFont font = new BitmapFont();
        font.getData().setScale(1.3f);

        TextButtonStyle btn = new TextButtonStyle();
        btn.font = font;
        btn.up = new TextureRegionDrawable(new TextureRegion(tex1x1));

        TextButton jugarBtn = boton("JUGAR", btn, 0.25f, 0.6f, 0.25f);
        jugarBtn.addListener(click(() -> start(true, 60f))); // MODO FIJO

        Table root = new Table();
        root.setFillParent(true);
        root.center();

        root.add(logoImg).padBottom(40);
        root.row();
        root.add(jugarBtn).width(300).height(60);

        stage.addActor(root);

        if (Recursos.musicaMenu != null) Recursos.musicaMenu.play();
    }

    private TextButton boton(String txt, TextButtonStyle st, float r, float g, float b) {
        TextButton btt = new TextButton(txt, st);
        btt.setColor(r,g,b,0.95f);
        btt.getLabel().setColor(1,1,1,1);
        return btt;
    }

    private ClickListener click(Runnable r) {
        return new ClickListener(){ @Override public void clicked(InputEvent e, float x, float y){ r.run(); } };
    }

    private void start(boolean extra, float seg) {
        if (Recursos.musicaMenu != null) Recursos.musicaMenu.stop();
        Recursos.liberar();
        // extra => modoBonus=true y modoExtra=true ; clásico => ambos false
        app.setScreen(new JuegoPantalla(app, seg, extra, extra));

        dispose();
    }

    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta); stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w,h,true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (tex1x1 != null) tex1x1.dispose();
    }
}
