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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
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

    @Override public void show() {
        Recursos.cargar();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // 1x1
        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pm.setColor(1,1,1,1); pm.fill();
        tex1x1 = new Texture(pm); pm.dispose();

        // Logo
        if (Recursos.logo != null) {
            logoImg = new Image(new TextureRegionDrawable(new TextureRegion(Recursos.logo)));
            float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
            float maxW = w * 0.7f, maxH = h * 0.35f;
            float esc = Math.min(1f, Math.min(maxW / Recursos.logo.getWidth(), maxH / Recursos.logo.getHeight()));
            logoImg.setSize(Recursos.logo.getWidth()*esc, Recursos.logo.getHeight()*esc);
        } else {
            logoImg = new Image(new TextureRegionDrawable(new TextureRegion(tex1x1)));
            logoImg.setColor(0.2f,0.2f,0.2f,1f);
            logoImg.setSize(320,120);
        }

        // Estilos
        BitmapFont font = new BitmapFont(); font.getData().setScale(1.2f);
        LabelStyle title = new LabelStyle(font, com.badlogic.gdx.graphics.Color.WHITE);

        TextButtonStyle btn = new TextButtonStyle();
        btn.font = font;
        btn.up = new TextureRegionDrawable(new TextureRegion(tex1x1));
        btn.over = btn.up; btn.down = btn.up;

        // Helpers
        TextButton bC60 = boton("Clásico 1:00 por turno", btn, 0.15f,0.6f,0.2f);
        TextButton bC30 = boton("Clásico 0:30 por turno", btn, 0.6f,0.5f,0.15f);
        TextButton bC15 = boton("Clásico 0:15 por turno", btn, 0.6f,0.2f,0.2f);

        TextButton bE60 = boton("EXTRA 1:00 (bonus + peón evo)", btn, 0.25f,0.45f,0.85f);
        TextButton bE30 = boton("EXTRA 0:30 (bonus + peón evo)", btn, 0.35f,0.35f,0.85f);
        TextButton bE15 = boton("EXTRA 0:15 (bonus + peón evo)", btn, 0.45f,0.25f,0.85f);

        // Navegación
        bC60.addListener(click(() -> start(false, 60f)));
        bC30.addListener(click(() -> start(false, 30f)));
        bC15.addListener(click(() -> start(false, 15f)));

        bE60.addListener(click(() -> start(true, 60f)));
        bE30.addListener(click(() -> start(true, 30f)));
        bE15.addListener(click(() -> start(true, 15f)));

        // Layout
        Table root = new Table(); root.setFillParent(true); root.top().padTop(20);
        Table logoRow = new Table(); logoRow.add(logoImg).center();
        root.add(logoRow).growX(); root.row();

        // Panel Clásico
        Table pClasico = new Table();
        pClasico.pad(12);
        pClasico.add(new Label("Modos CLÁSICOS", title)).colspan(1).padBottom(10);
        pClasico.row();
        pClasico.add(bC60).width(320).height(48).pad(5); pClasico.row();
        pClasico.add(bC30).width(320).height(48).pad(5); pClasico.row();
        pClasico.add(bC15).width(320).height(48).pad(5);

        // Panel Extra
        Table pExtra = new Table();
        pExtra.pad(12);
        pExtra.add(new Label("Modos EXTRA", title)).colspan(1).padBottom(10);
        pExtra.row();
        pExtra.add(bE60).width(360).height(48).pad(5); pExtra.row();
        pExtra.add(bE30).width(360).height(48).pad(5); pExtra.row();
        pExtra.add(bE15).width(360).height(48).pad(5);

        Table cols = new Table();
        cols.add(pClasico).pad(10);
        cols.add(pExtra).pad(10);

        root.row(); root.add(cols).center();
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
    @Override public void dispose() { if (stage!=null) stage.dispose(); if (tex1x1!=null) tex1x1.dispose(); }
}
