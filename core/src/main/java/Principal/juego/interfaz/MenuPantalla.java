package Principal.juego.interfaz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import Principal.juego.Principal;
import Principal.juego.utiles.Recursos;

public class MenuPantalla implements Screen {

    private final Principal app;

    private Stage stage;
    private Texture tex1x1; // para fondos/botones
    private Image logoImg;

    public MenuPantalla(Principal app) { this.app = app; }

    @Override
    public void show() {
        Recursos.cargar(); // logo + música (si existe)

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // 1x1 blanca para tintar
        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pm.setColor(1,1,1,1);
        pm.fill();
        tex1x1 = new Texture(pm);
        pm.dispose();

        // Logo escalado (hasta 70% del ancho/alto, manteniendo proporción)
        if (Recursos.logo != null) {
            logoImg = new Image(new TextureRegionDrawable(new TextureRegion(Recursos.logo)));
            float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
            float maxW = w * 0.7f, maxH = h * 0.35f;
            float escX = maxW / Recursos.logo.getWidth();
            float escY = maxH / Recursos.logo.getHeight();
            float escala = Math.min(1f, Math.min(escX, escY));
            logoImg.setSize(Recursos.logo.getWidth()*escala, Recursos.logo.getHeight()*escala);
        } else {
            logoImg = new Image(new TextureRegionDrawable(new TextureRegion(tex1x1)));
            logoImg.setColor(0.2f,0.2f,0.2f,1f);
            logoImg.setSize(320,120);
        }

        // Estilos de texto y botones
        BitmapFont font = new BitmapFont();
        font.getData().setScale(1.2f);
        LabelStyle titleStyle = new LabelStyle(font, com.badlogic.gdx.graphics.Color.WHITE);

        TextButtonStyle btnStyle = new TextButtonStyle();
        btnStyle.font = font;
        btnStyle.up   = new TextureRegionDrawable(new TextureRegion(tex1x1));
        btnStyle.over = new TextureRegionDrawable(new TextureRegion(tex1x1));
        btnStyle.down = new TextureRegionDrawable(new TextureRegion(tex1x1));
        // colores
        ((TextureRegionDrawable)btnStyle.up).getRegion().getTexture().bind();
        // tintas:
        // up
        btnStyle.up.setLeftWidth(0);
        btnStyle.up.setRightWidth(0);
        // usamos setColor en los botones

        // Botones
        TextButton b60 = new TextButton("Jugar 1:00 por turno", btnStyle);
        TextButton b30 = new TextButton("Jugar 0:30 por turno", btnStyle);
        TextButton b15 = new TextButton("Jugar 0:15 por turno", btnStyle);

        // Colores por botón
        tintButton(b60, 0.15f, 0.6f, 0.2f);
        tintButton(b30, 0.6f, 0.5f, 0.15f);
        tintButton(b15, 0.6f, 0.2f, 0.2f);

        // Listeners
        b60.addListener(click(() -> startGame(60f)));
        b30.addListener(click(() -> startGame(30f)));
        b15.addListener(click(() -> startGame(15f)));

        // Layout con Table
        Table root = new Table();
        root.setFillParent(true);
        root.top().padTop(20);

        // Fila logo
        Table logoRow = new Table();
        logoRow.add(logoImg).center();
        root.add(logoRow).growX();
        root.row();

        // Sombra/fondo translúcido para el bloque de botones
        Image bgPanel = new Image(new TextureRegionDrawable(new TextureRegion(tex1x1)));
        bgPanel.setColor(0f,0f,0f,0.35f);

        Table panel = new Table();
        panel.pad(16);
        panel.add(new Label("Elegí el tiempo por turno", titleStyle)).padBottom(12).colspan(1).center();
        panel.row();
        panel.add(b60).width(280).height(48).pad(6);
        panel.row();
        panel.add(b30).width(280).height(48).pad(6);
        panel.row();
        panel.add(b15).width(280).height(48).pad(6);

        Table panelWrap = new Table();
        panelWrap.add(bgPanel).grow();
        panelWrap.add(panel).center().pad(10);
        panelWrap.left(); // que el bg se estire por detrás

        // Fila panel
        root.row();
        root.add(panelWrap).padTop(18).center();

        stage.addActor(root);

        if (Recursos.musicaMenu != null) Recursos.musicaMenu.play();
    }

    private void tintButton(TextButton b, float r, float g, float bl) {
        b.setColor(r,g,bl,0.95f); // color fondo (usa drawable 1x1 tintado)
        b.getLabel().setColor(1f,1f,1f,1f);
    }

    private ClickListener click(Runnable r) {
        return new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { r.run(); }
        };
    }

    private void startGame(float segundosPorTurno) {
        if (Recursos.musicaMenu != null) Recursos.musicaMenu.stop();
        Recursos.liberar();
        app.setScreen(new JuegoPantalla(segundosPorTurno));
        dispose();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (tex1x1 != null) tex1x1.dispose();
    }
}
