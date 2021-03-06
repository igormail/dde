package ua.com.integer.dde.extension.ui.editor;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import javax.swing.JOptionPane;

import ua.com.integer.dde.extension.ui.UiConfig;
import ua.com.integer.dde.extension.ui.UiConfigurator;
import ua.com.integer.dde.extension.ui.property.util.actor.ActorUtils;
import ua.com.integer.dde.res.screen.AbstractScreen;
import ua.com.integer.dde.res.screen.ScreenEvent;
import ua.com.integer.dde.res.screen.ScreenListener;
import ua.com.integer.dde.startpanel.Settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class UiEditorScreen extends AbstractScreen implements ConfigChangedListener {
	private Label messageLabel;

	private static final Color STAGE_BORDER_COLOR = Color.RED;
	private static final Color UNSELECTED_ACTOR_COLOR = Color.GREEN;
	
	private Color selectedActorColor = Color.WHITE;
	
	private Actor selectedActor;
	
	private ShapeRenderer shapeRenderer;
	
	private UiConfig config;
	
	private ConfigChangedListener configChangedListener;
	
	class SwitchColorTask extends Task {
		@Override
		public void run() {
			if (selectedActorColor == Color.WHITE) {
				selectedActorColor = Color.BLACK;
			} else {
				selectedActorColor = Color.WHITE;
			}
		}
	}
	
	private boolean drawStageBorders = true;
	private boolean drawRectangleAroundSelectedActor = true;
	private boolean drawRectanglesAroundUnselectedActors = true;
	
	public UiEditorScreen() {
		Timer.schedule(new SwitchColorTask(), 0, 0.2f);
		shapeRenderer = new ShapeRenderer();
		
		addDefaultConfig();
		updateHighlightActorSettings();
	}
	
	private void addDefaultConfig() {
		UiConfig config = new UiConfig();
		config.name = "root";
		configChanged(config);
		selectActorByConfig(config, getStage().getRoot());
	}
	
	public void updateHighlightActorSettings() {
		Settings sets = Settings.getInstance();
		sets.setSettingsClass(UiEditorScreen.class);
		
		drawRectangleAroundSelectedActor = sets.getBoolean("highlight-active-actor", true);
		drawRectanglesAroundUnselectedActors = sets.getBoolean("highlight-inactive-actors", true);
		drawStageBorders = sets.getBoolean("draw-stage-borders", true);
	}
	
	public void setConfigChangedListener(ConfigChangedListener configChangedListener) {
		this.configChangedListener = configChangedListener;
	}
	
	/**
	 * Выбирает актера для подсветки. Линии вокруг него будут мигать
	 * @param selectedActor
	 */
	public void selectActor(Actor selectedActor) {
		boolean needUpdateProperties = 
				this.selectedActor == null ||
				this.selectedActor.getUserObject() != selectedActor.getUserObject();
		
		this.selectedActor = selectedActor;
		getStage().setScrollFocus(selectedActor);
		
		if (needUpdateProperties && EditorKernel.getInstance().getActorListDialog() != null) {
			EditorKernel.getInstance().getActorListDialog().updatePropertyPanelForSelectedActor();
		}
	}
	
	/**
	 * Рекурсивно ищет актера, у которого свойство getUserObject будет равняться переданному конфигурационному файлу
	 */
	public void selectActorByConfig(UiConfig cfg) {
		selectActorByConfig(cfg, getStage().getRoot());
	}
	
	private void selectActorByConfig(UiConfig cfg, Actor actor) {
		if (actor.getUserObject() == cfg) {
			selectActor(actor);
			return;
		}
		
		if (actor instanceof Group) {
			for(Actor child : ((Group) actor).getChildren()) {
				selectActorByConfig(cfg, child);
			}
		}
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);
		
		shapeRenderer.setProjectionMatrix(getStage().getBatch().getProjectionMatrix());
		shapeRenderer.setTransformMatrix(getStage().getBatch().getTransformMatrix());
		
		shapeRenderer.begin(ShapeType.Line);
			drawLinesAroundActor(getStage().getRoot());
		shapeRenderer.end();
	}
	
	private void drawLinesAroundActor(Actor actor) {
		recursivelyDrawLinesAroundActor(actor);
		if (drawStageBorders) {
			shapeRenderer.setColor(STAGE_BORDER_COLOR);
			shapeRenderer.rect(getStage().getRoot().getX(), getStage().getRoot().getY(), 
					getStage().getRoot().getWidth(), getStage().getRoot().getHeight());
		}
		
		drawSideLineFromSelectedActor();
	}
	
	private void drawSideLineFromSelectedActor() {
		if (getSelectedActor() == null) return;
		if (getSelectedActor().getParent() == null) return;
		if (!drawRectangleAroundSelectedActor) return;
		
		float dx = getStage().getRoot().getX();
		float dy = getStage().getRoot().getY();
		
		Vector2 realCoordsOfSelectedActor = ((Group) getSelectedActor().getParent()).localToStageCoordinates(new Vector2(getSelectedActor().getX(), getSelectedActor().getY()));
		
		float actorX = realCoordsOfSelectedActor.x;
		float actorY = realCoordsOfSelectedActor.y;
		float actorWidth = getSelectedActor().getWidth();
		float actorHeight = getSelectedActor().getHeight();
		
		float parentX = actorX - getSelectedActor().getX();
		float parentY = actorY - getSelectedActor().getY();
		float parentWidth = getSelectedActor().getParent().getWidth();
		float parentHeight = getSelectedActor().getParent().getHeight();
		
		shapeRenderer.setColor(Color.BLUE);

		switch(getSelectedConfig().parentCorner) {
		case BOTTOM_LEFT :
			shapeRenderer.line(dx + actorX, dy + actorY, dx + parentX, dy + parentY);
			break;
		case TOP_LEFT :
			shapeRenderer.line(dx + actorX, dy + actorY + actorHeight, dx + parentX, dy + parentY + parentHeight );
			break;
		case BOTTOM_RIGHT :
			shapeRenderer.line(dx + actorX + actorWidth, dy + actorY, dx + parentX + parentWidth, dy + parentY);
			break;
		case TOP_RIGHT :
			shapeRenderer.line(dx + actorX + actorWidth, dy + actorY + actorHeight, dx + parentX + parentWidth, dy + parentY + parentHeight);
			break;
		case CENTER :
			shapeRenderer.setColor(Color.RED);
			shapeRenderer.circle(dx + actorX + actorWidth/2, dy + actorY + actorHeight/2, 1);
			shapeRenderer.setColor(Color.BLUE);
			shapeRenderer.circle(dx + parentX + parentWidth/2, dy + parentY + parentHeight/2, 1);
			shapeRenderer.line(dx + actorX + actorWidth/2, dy + actorY + actorHeight/2, dx + parentX + parentWidth/2, dy + parentY + parentHeight/2);
			break;
		}
	}
	
	private void recursivelyDrawLinesAroundActor(Actor actor) {
		float dx = getStage().getRoot().getX();
		float dy = getStage().getRoot().getY();
		
		if (actor == selectedActor) {
			shapeRenderer.setColor(selectedActorColor);
		} else {
			shapeRenderer.setColor(UNSELECTED_ACTOR_COLOR);
		}
		if (actor.getParent() != null) {
			Vector2 realCoords = ((Group) actor.getParent()).localToStageCoordinates(new Vector2(actor.getX(), actor.getY()));
			if (actor == selectedActor) {
				if (drawRectangleAroundSelectedActor) {
					shapeRenderer.rect(dx + realCoords.x, dy + realCoords.y, actor.getOriginX(), actor.getOriginY(), actor.getWidth(), actor.getHeight(), actor.getScaleX(), actor.getScaleY(), getFullRotationForActor(actor));
				}
			} else if (drawRectanglesAroundUnselectedActors && actor.getUserObject() instanceof UiConfig) {
				shapeRenderer.rect(dx + realCoords.x, dy + realCoords.y, actor.getOriginX(), actor.getOriginY(), actor.getWidth(), actor.getHeight(), actor.getScaleX(), actor.getScaleY(), getFullRotationForActor(actor));
			}
		}

		if (actor instanceof Group) {
			for(Actor child : ((Group) actor).getChildren()) {
				recursivelyDrawLinesAroundActor(child);
			}
		}
	}

	@Override
	public void configChanged(UiConfig config) {
		if (this.config != null) {
			setConfig(this.config);
			selectActorByConfig(config, getStage().getRoot().getChildren().first());
		} else {
			setConfig(config);
		}
	}
	
	/**
	 * "Разворачивает" актера из его конфига в корневой актер экрана
	 * @param config
	 */
	public void setConfig(final UiConfig config) {
		Gdx.app.postRunnable(new Runnable() {
			public void run() {
				UiConfig toSelect = null;
				if (selectedActor != null) {
					toSelect = (UiConfig) selectedActor.getUserObject();
				}
				
				try {
					UiEditorScreen.this.config = config;
					
					clearScreenListeners();
					
					getStage().clear();
					getStage().getRoot().addListener(new StageRootDragListener());
					
					ActorUtils.deployConfigToScreen(UiEditorScreen.this, config);
					
					addListenerRecursive(getStage().getRoot());
					
					if (toSelect == null) {
						selectActorByConfig(config, getStage().getRoot());
					} else {
						selectActorByConfig(toSelect, getStage().getRoot());
						
						if (selectedActor == null) {
							selectActorByConfig(config, getStage().getRoot());
						}
					}
					
					
					if (configChangedListener != null) {
						configChangedListener.configChanged(config);
					}
					
					notifyAboutEvent(ScreenEvent.SHOW);
				} catch(Exception ex) {
					if (getUiConfig() != null) {
						selectActorByConfig(getUiConfig());
					}
					ex.printStackTrace();

					JOptionPane.showMessageDialog(null, "Config incorrect!");
				}
				
				EditorKernel.getInstance().getActorListDialog().updateActorTree();
			}
		});
		
	}
	
	/**
	 * Рекурсивно добавляет actor и его детям (если они есть) слушатель на 
	 * редактирование
	 */
	private void addListenerRecursive(Actor actor) {
		if (actor.getUserObject() != null) {
			actor.addListener(new ActorInputListener(this));
		}
		
		if (actor instanceof Group) {
			for(Actor child: ((Group) actor).getChildren()) {
				addListenerRecursive(child);
			}
		}
	}
	
	/**
	 * Обновляет текущий конфиг
	 */
	public void updateConfig() {
		setConfig(this.config);
	}

	/**
	 * Возвращает текущий конфиг
	 */
	public UiConfig getUiConfig() {
		return config;
	}
	
	/**
	 * Рекурсивно ищет и удаляет toDelete из container
	 */
	private void removeConfig(UiConfig container, UiConfig toDelete) {
		for(UiConfig cfg : container.children) {
			if (cfg == toDelete) {
				container.children.removeValue(toDelete, true);
				updateConfig();
				selectActorByConfig(container, getStage().getRoot());
				return;
			} else {
				removeConfig(cfg, toDelete);
			}
		}
		
	}
	
	/**
	 * Удаляет config из иерархии и обновляет экран. 
	 * Если предпринимается попытка удаления корневого обьекта, 
	 * выдается сообщение про ошибку
	 */
	public void removeUiConfig(UiConfig config) {
		if (config == null) return;
		
		if (this.config == config) {
			JOptionPane.showMessageDialog(null, "Can't delete root actor!");
		} else {
			removeConfig(getUiConfig(), config);
		}
	}

	/**
	 * Возвращает выбранного актера или null в случае его отсутствия
	 */
	public Actor getSelectedActor() {
		return selectedActor;
	}
	
	private float getFullRotationForActor(Actor actor) {
		if (actor.getParent() == null) {
			return actor.getRotation();
		} else {
			return actor.getRotation() + getFullRotationForActor(actor.getParent());
		}
	}

	public UiConfig getSelectedConfig() {
		return (UiConfig) getSelectedActor().getUserObject();
	}

	public void selectRoot() {
		selectActorByConfig(config);
	}
	
	public UiConfigurator getConfiguratorForConfig(UiConfig config) {
		for(ScreenListener listener : getScreenListeners()) {
			if (listener instanceof UiConfigurator) {
				UiConfigurator configurator = (UiConfigurator) listener;
				if (configurator.getConfig() == config) {
					return configurator;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Показывает сообщение, которое через секунду исчезает. 
	 * Если на экране было старое сообщение, оно заменяется новым
	 * 
	 * @param text текст сообщения
	 */
	public void showMessage(String text) {
		if (messageLabel != null && messageLabel.getParent() != null) {
			messageLabel.remove();
		}
		
		LabelStyle lStyle = new LabelStyle(getFont("standard", 20), Color.GREEN);
		messageLabel = new Label(text, lStyle);
		messageLabel.setPosition((getStage().getRoot().getWidth() - messageLabel.getPrefWidth())/2 - getStage().getRoot().getX(), 
							     (getStage().getRoot().getHeight() - messageLabel.getHeight())/2 - getStage().getRoot().getY());
		addActor(messageLabel);
		
		messageLabel.addAction(sequence(delay(0.5f), fadeOut(0.2f), Actions.removeActor()));
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}
}
